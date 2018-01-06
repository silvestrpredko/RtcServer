package app;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketSessionDecorator;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Silvestr Predko
 */
public final class CallMember implements ConnectionTimerTask.TimerRunner {

  private WebSocketSessionDecorator session;
  private String clientId;
  private String callId;
  private String message;
  private ConnectionTimerTask connectionTimerTask;
  private ClientNotResponseListener listener;
  private Set<String> connectedClients = new HashSet<>();

  public CallMember(WebSocketSession session, String clientId, String callId, ClientNotResponseListener listener) {
    this.session = new WebSocketSessionDecorator(session);
    this.clientId = clientId;
    this.callId = callId;
    this.listener = listener;
  }

  public CallMember(WebSocketSession session, String clientId, String callId) {
    this.session = new WebSocketSessionDecorator(session);
    this.clientId = clientId;
    this.callId = callId;
  }

  private void initialize() {
    connectionTimerTask = new ConnectionTimerTask(this);
  }

  @Override
  public void run() {
    if (listener == null) {
      throw new RuntimeException("ClientNotResponseListener not initialized");
    }
    listener.onClientNotRespond(session, callId);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    CallMember that = (CallMember) o;

    return
        session != null && that.session != null && clientId != null && that.clientId != null &&
            session.getId().equals(that.session.getId()) && clientId.equals(that.clientId);
  }

  @Override
  public int hashCode() {
    int result = callId != null ? callId.hashCode() : 0;
    result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
    result = 31 * result + (message != null ? message.hashCode() : 0);
    return result;
  }

  public WebSocketSessionDecorator getSession() {
    return session;
  }

  public String getClientId() {
    return clientId;
  }

  public void setSession(WebSocketSessionDecorator session) {
    this.session = session;
  }

  public String getMessage() {
    return message;
  }

  public boolean isConnected(String clientId) {
    return connectedClients.contains(clientId);
  }

  public void setConnected(String clientId) {
    connectedClients.add(clientId);
  }

  public ConnectionTimerTask getConnectionTimerTask() {
    initialize();
    return connectionTimerTask;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void cancelWaiting() {
    if (this.connectionTimerTask != null) {
      this.connectionTimerTask.cancel();
    }
  }

  public interface ClientNotResponseListener {
    void onClientNotRespond(WebSocketSession session, String callId);
  }
}
