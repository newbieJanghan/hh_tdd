package io.hhplus.tdd.point;

import java.util.List;

public interface PointService {
  public UserPoint getUserPoint(long id);

  public List<PointHistory> getPointHistories(long id);

  public UserPoint charge(long id, long amount);

  public UserPoint use(long id, long amount);
}
