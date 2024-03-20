package io.hhplus.tdd.database;

import io.hhplus.tdd.point.model.UserPoint;
import java.util.HashMap;
import java.util.Map;

public class UserPointTableStub extends UserPointTable {
  private Map<Long, UserPoint> table = new HashMap<>();

  public void setTable(Map<Long, UserPoint> table) {
    this.table = table;
  }

  @Override
  public UserPoint selectById(Long id) {
    throttle(200);
    return table.getOrDefault(id, UserPoint.empty(id));
  }

  @Override
  public UserPoint insertOrUpdate(long id, long amount) {
    throttle(300);
    UserPoint userPoint = new UserPoint(id, amount, System.currentTimeMillis());
    table.put(id, userPoint);
    return userPoint;
  }

  private void throttle(long millis) {
    try {
      Thread.sleep((long) (Math.random() * millis));
    } catch (InterruptedException ignored) {
    }
  }
}
