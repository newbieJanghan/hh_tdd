package io.hhplus.tdd.database;

import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.TransactionType;
import java.util.ArrayList;
import java.util.List;

public class PointHistoryTableStub extends PointHistoryTable {
  private List<PointHistory> table = new ArrayList<>();
  private long cursor = 1;

  public void setTable(List<PointHistory> table) {
    this.table = table;
  }

  @Override
  public List<PointHistory> selectAllByUserId(long userId) {
    return table.stream().filter(pointHistory -> pointHistory.userId() == userId).toList();
  }

  @Override
  public PointHistory insert(long userId, long amount, TransactionType type, long updateMillis) {
    throttle(300L);
    PointHistory pointHistory = new PointHistory(cursor++, userId, amount, type, updateMillis);
    table.add(pointHistory);
    return pointHistory;
  }

  private void throttle(long millis) {
    try {
      Thread.sleep((long) (Math.random() * millis));
    } catch (InterruptedException ignored) {
    }
  }
}
