package app;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Silvestr Predko
 */
public class Calls {

  private static volatile Calls instance = new Calls();
  private Map<String, Call> callsMap = new ConcurrentHashMap<>();

  public static Calls getInstance() {
    return instance;
  }

  private Calls() {}

  public void addCall(@NotNull Call call, String callId) {
    callsMap.put(callId, call);
  }

  public void removeCall(String callId) {
    callsMap.remove(callId);
  }

  public void removeAllCalls() {
    callsMap.clear();
  }

  public Call getCall(String callId) {
    return callsMap.get(callId);
  }
}
