package io.hhplus.tdd.point;

import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.service.PointServiceImpl;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/point")
public class PointController {

  private final PointServiceImpl pointService;
  private static final Logger log = LoggerFactory.getLogger(PointController.class);

  @Autowired
  PointController(@Qualifier("lockPointServiceImpl") PointServiceImpl pointService) {
    this.pointService = pointService;
  }

  @GetMapping("{id}")
  public UserPoint point(@PathVariable long id) {
    return pointService.getUserPoint(id);
  }

  @GetMapping("{id}/histories")
  public List<PointHistory> history(@PathVariable long id) {
    return pointService.getPointHistories(id);
  }

  @PatchMapping("{id}/charge")
  public UserPoint charge(@PathVariable long id, @RequestBody long amount)
      throws InterruptedException {
    return pointService.charge(id, amount);
  }

  @PatchMapping("{id}/use")
  public UserPoint use(@PathVariable long id, @RequestBody long amount) {
    return pointService.use(id, amount);
  }
}
