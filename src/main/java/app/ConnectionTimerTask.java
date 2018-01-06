package app;

import java.util.TimerTask;

/**
 * @author Silvestr Predko
 */
public class ConnectionTimerTask extends TimerTask {

  private TimerRunner runner;

  public ConnectionTimerTask(TimerRunner runner) {
    this.runner = runner;
  }

  @Override
  public void run() {
    this.runner.run();
  }

  public interface TimerRunner {
    void run();
  }
}
