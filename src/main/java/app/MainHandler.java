package app;

import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import static proto.Proto.*;

/**
 * @author Silvestr Predko
 */
public class MainHandler extends TextWebSocketHandler {

  private Timer timer = new Timer("Main Timer", true);

  @Override
  protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
    try {
      handleMessage(message, session);
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

  }

  private Boolean handleMessage(BinaryMessage message, WebSocketSession session) throws InvalidProtocolBufferException {
    final MessageContainer messageContainer = MessageContainer.parseFrom(message.getPayload());
    switch (messageContainer.getMessageType()) {
      case CallRequest: {
        handleCallRequest(CallRequest.parseFrom(messageContainer.getMessage()), session);
        return true;
      }
      case Peer: {
        handlePeer(Peer.parseFrom(messageContainer.getMessage()), session);
        return true;
      }
      case Session: {
        final Session rtcSession = Session.parseFrom(messageContainer.getMessage());
        if (rtcSession.getSessionType() == Session.Type.ANSWER) {
          handleAnswer(rtcSession);
        } else {
          handleOffer(rtcSession);
        }
        return true;
      }
      case IceServers: {
        handleIceServers(IceServers.parseFrom(messageContainer.getMessage()));
        return true;
      }
    }

    return false;
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    Thread.sleep(1000);
    session.sendMessage(new TextMessage(new String(message.asBytes()) + "Hello From Server"));
  }

  private void handleCallRequest(CallRequest callRequest, WebSocketSession session) {
    final Calls calls = Calls.getInstance();
    final Call call = calls.getCall(callRequest.getCallId());

    final CallMember creator = new CallMember(
        session,
        callRequest.getLocalClientId(),
        callRequest.getCallId(),
        MainHandler::clientNotRespond
    );

    System.out.println("Handle Call Request");

    if (call != null) {
      call.getCallMembers().forEach((item) -> {
        item.cancelWaiting();
        sendCallError(item.getSession(), call.getCallId());
      });
      calls.removeCall(callRequest.getCallId());
    }

    Call newCall = new Call(callRequest.getCallId());

    newCall.addCallMember(creator);
    calls.addCall(newCall, callRequest.getCallId());

    timer.schedule(creator.getConnectionTimerTask(), TimeUnit.SECONDS.toMillis(20));
  }

  private void handlePeer(Peer peer, WebSocketSession session) {
    final Call call = Calls.getInstance().getCall(peer.getCallId());

    if (call == null) {
      return;
    }

    call.getCallMembers().forEach(CallMember::cancelWaiting);

    System.out.println("Handle Peer");

    call.addCallMember(new CallMember(session, peer.getLocalClientId(), peer.getCallId()));
    call.getCallMembers()
        .stream()
        .filter(callMember -> !callMember.getClientId().equals(peer.getLocalClientId()))
        .forEach(callMember -> sendPeer(callMember.getSession(), peer));
  }

  private void handleIceServers(IceServers iceServers) {
    final Call call = Calls.getInstance().getCall(iceServers.getCallId());

    if (call == null) {
      return;
    }

    CallMember member = call.getCallMembers()
        .stream()
        .filter(callMember -> callMember.getClientId().equals(iceServers.getRemoteClientId()))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("No Peer By this Id"));

    sendIceServers(member.getSession(), iceServers);
  }

  private void handleOffer(Session offer) {
    final Call call = Calls.getInstance().getCall(offer.getCallId());

    if (call == null) {
      return;
    }

    System.out.println("Handle Offer");

    CallMember member = call.getCallMembers()
        .stream()
        .filter(callMember -> callMember.getClientId().equals(offer.getRemoteClientId()))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("No Peer By this Id"));

    sendOffer(member.getSession(), offer);
  }

  private void handleAnswer(Session answer) {
    final Call call = Calls.getInstance().getCall(answer.getCallId());

    if (call == null) {
      return;
    }

    System.out.println("Handle Answer");

    CallMember member = call.getCallMembers()
        .stream()
        .filter(callMember -> callMember.getClientId().equals(answer.getRemoteClientId()))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("No Peer By this Id"));

    sendAnswer(member.getSession(), answer);
  }

  public static void sendIceServers(WebSocketSession session, IceServers iceServers) {
    try {
      session.sendMessage(MessageBuilder.wrapIceServers(iceServers));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void sendPeer(WebSocketSession session, Peer peer) {
    try {
      System.out.println("[Send Peer" + peer.getLocalClientId() + "]");
      session.sendMessage(MessageBuilder.wrapPeer(peer));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void sendOffer(WebSocketSession session, Session offer) {
    try {
      session.sendMessage(MessageBuilder.wrapOffer(offer));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void sendAnswer(WebSocketSession session, Session answer) {
    try {
      session.sendMessage(MessageBuilder.wrapAnswer(answer));
    } catch (IOException e) {
      // implement error response for client
      e.printStackTrace();
    }
  }

  public static void clientNotRespond(WebSocketSession session, String callId) {
    try {
      final BinaryMessage message = MessageBuilder.createNotRespondMessage(callId);
      if (session != null && session.isOpen()) {
        session.sendMessage(message);
        session.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      Calls.getInstance().removeCall(callId);
    }
  }

  public static void sendCallError(WebSocketSession session, String callId) {
    try {
      final BinaryMessage message = MessageBuilder.createErrorMessage(callId);
      if (session != null && session.isOpen()) {
        session.sendMessage(message);
        session.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
