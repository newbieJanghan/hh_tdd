package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

@Service
public class PointServiceImpl implements PointService {
  private final UserPointTable userPointTable;
  private final PointHistoryTable pointHistoryTable;

  public PointServiceImpl(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
    this.userPointTable = userPointTable;
    this.pointHistoryTable = pointHistoryTable;
  }

  //        public UserPoint point(long id) {
  //            return UserPoint.empty(id);
  //        }
  //
  //        public List<PointHistory> history(long id) {
  //            return List.of();
  //        }

  public UserPoint get(long id) {
    return userPointTable.selectById(id);
  }

  public UserPoint charge(long id, long amount) {
    validateUserPointAmount(amount);

    UserPoint currentUserPoint = userPointTable.selectById(id);
    PointHistory pointHistory =
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

    return userPointTable.insertOrUpdate(id, currentUserPoint.point() + pointHistory.amount());
  }

  public UserPoint use(long id, long amount) {
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