package app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Silvestr Predko
 */
public class Call {

  private ReentrantLock lock = new ReentrantLock(true);

  public Call(String callId) {
    this.callId = callId;
  }

  private List<CallMember> callMembers = new ArrayList<>();
  private String callId;

  public void addCallMember(CallMember callMember) {
    try {
      lock.lock();
      callMembers.add(callMember);
    } finally {
      lock.unlock();
    }
  }

  public void removeCallMember(CallMember callMember) {
    while (lock.isLocked()) {
    }
    callMembers.remove(callMember);
  }

  public List<CallMember> getCallMembers() {
    while (lock.isLocked()) {
    }
    return callMembers;
  }

  public String getCallId() {
    return callId;
  }
}
