package io.hhplus.tdd.point;

public interface PointService {
  public UserPoint get(long id);

  public UserPoint charge(long id, long amount);

  public UserPoint use(long id, long amount);
}
