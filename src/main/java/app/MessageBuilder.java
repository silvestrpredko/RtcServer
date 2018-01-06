package app;

import org.springframework.web.socket.BinaryMessage;

import static proto.Proto.*;

/**
 * @author Silvestr Predko
 */
public class MessageBuilder {
  public static BinaryMessage createNotRespondMessage(String callId) {
    return new BinaryMessage(
        MessageContainer.newBuilder()
            .setMessageType(MessageContainer.MessageType.CallRequestError)
            .setMessage(
                CallRequestError.newBuilder()
                    .setCallId(callId)
                    .setStatus(CallRequestError.ResponseStatus.TIMEOUT)
                    .build()
                    .toByteString()
            ).build().toByteArray()
    );
  }

  public static BinaryMessage createErrorMessage(String callId) {
    return new BinaryMessage(
        MessageContainer.newBuilder()
            .setMessageType(MessageContainer.MessageType.CallRequestError)
            .setMessage(
                CallRequestError.newBuilder()
                    .setCallId(callId)
                    .setStatus(CallRequestError.ResponseStatus.ERROR)
                    .build()
                    .toByteString()
            ).build().toByteArray()
    );
  }

  public static BinaryMessage wrapPeer(Peer peer) {
    return new BinaryMessage(
        MessageContainer.newBuilder()
            .setMessageType(MessageContainer.MessageType.Peer)
            .setMessage(peer.toByteString())
            .build()
            .toByteArray()
    );
  }

  public static BinaryMessage wrapOffer(Session offer) {
    return new BinaryMessage(
        MessageContainer.newBuilder()
            .setMessageType(MessageContainer.MessageType.Session)
            .setMessage(offer.toByteString())
            .build()
            .toByteArray()
    );
  }

  public static BinaryMessage wrapAnswer(Session answer) {
    return new BinaryMessage(
        MessageContainer.newBuilder()
            .setMessageType(MessageContainer.MessageType.Session)
            .setMessage(answer.toByteString())
            .build()
            .toByteArray()
    );
  }
}
