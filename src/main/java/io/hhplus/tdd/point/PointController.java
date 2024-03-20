package io.hhplus.tdd.point;

import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.service.PointServiceImpl;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/point")
public class PointController {

  private final PointServiceImpl pointService;
  private static final Logger log = LoggerFactory.getLogger(PointController.class);

  @Autowired
  PointController(PointServiceImpl pointService) {
    this.pointService = pointService;
  }

  /** TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요. */
  @GetMapping("{id}")
  public UserPoint point(@PathVariable long id) {
    return pointService.getUserPoint(id);
  }

  /** TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요. */
  @GetMapping("{id}/histories")
  public List<PointHistory> history(@PathVariable long id) {
    return pointService.getPointHistories(id);
  }

  /** TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요. */
  @PatchMapping("{id}/charge")
  public UserPoint charge(@PathVariable long id, @RequestBody long amount) {
    return pointService.charge(id, amount);
  }

  /** TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요. */
  @PatchMapping("{id}/use")
  public UserPoint use(@PathVariable long id, @RequestBody long amount) {
    return pointService.use(id, amount);
  }
}
