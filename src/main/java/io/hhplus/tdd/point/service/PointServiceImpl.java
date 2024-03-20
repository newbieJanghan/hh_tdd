package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.exceptions.InsufficientPointsException;
import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.TransactionType;
import io.hhplus.tdd.point.model.UserPoint;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointServiceImpl implements PointService {
  private final UserPointTable userPointTable;
  private final PointHistoryTable pointHistoryTable;

  @Autowired
  public PointServiceImpl(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
    this.userPointTable = userPointTable;
    this.pointHistoryTable = pointHistoryTable;
  }

  public UserPoint getUserPoint(long userId) {
    return userPointTable.selectById(userId);
  }

  public List<PointHistory> getPointHistories(long userId) {

    return pointHistoryTable.selectAllByUserId(userId);
  }

  public UserPoint charge(long id, long amount) throws IllegalArgumentException {
    validateUserPointAmount(amount);

    UserPoint currentUserPoint = userPointTable.selectById(id);
    PointHistory pointHistory =
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

    return userPointTable.insertOrUpdate(id, currentUserPoint.point() + pointHistory.amount());
  }

  public UserPoint use(long id, long amount)
      throws IllegalArgumentException, InsufficientPointsException {
    validateUserPointAmount(amount);

    UserPoint currentUserPoint = userPointTable.selectById(id);
    if (currentUserPoint.point() < amount) {
      throw new InsufficientPointsException();
    }

    PointHistory pointHistory =
        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

    return userPointTable.insertOrUpdate(id, currentUserPoint.point() - pointHistory.amount());
  }

  private void validateUserPointAmount(long amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("amount should be positive");
    }
  }
}
