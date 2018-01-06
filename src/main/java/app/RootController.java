package app;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Silvestr Predko
 */
@RestController
public class RootController {
  @RequestMapping("/")
  public String greeting() {
    return "Please use WebSockets for communication";
  }
}
