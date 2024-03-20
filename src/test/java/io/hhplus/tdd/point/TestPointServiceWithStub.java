package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.*;

import io.hhplus.tdd.database.PointHistoryTableStub;
import io.hhplus.tdd.database.UserPointTableStub;
import io.hhplus.tdd.point.exceptions.InsufficientPointsException;
import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.TransactionType;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.service.PointServiceImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestPointServiceWithStub {
  private UserPointTableStub userPointTableStub;
  private PointHistoryTableStub pointHistoryTableStub;
  private PointServiceImpl pointService;

  @BeforeEach
  public void setUp() {
    userPointTableStub = new UserPointTableStub();
    pointHistoryTableStub = new PointHistoryTableStub();
    pointService = new PointServiceImpl(userPointTableStub, pointHistoryTableStub);
  }

  @Test
  public void getDefaultUserPoint_ByUserId_WhenNotExist() {
    long userId = 1;

    // when
    UserPoint userPoint = pointService.getUserPoint(userId);

    // then
    assertEquals(userId, userPoint.id());
    assertEquals(0, userPoint.point());
    assertNotEquals(System.currentTimeMillis(), userPoint.updateMillis());
  }

  @Test
  public void getUserPoint_ByUserId() {
    long userId = 1;

    // given
    HashMap<Long, UserPoint> table = new HashMap<>();
    table.put(userId, new UserPoint(userId, 100, System.currentTimeMillis()));

    userPointTableStub.setTable(table);

    // when
    UserPoint userPoint = pointService.getUserPoint(userId);

    // then
    assertEquals(userId, userPoint.id());
    assertEquals(100, userPoint.point());
  }

  @Test
  public void getPointHistories_ByUserId() {
    long userId = 1;
    long otherUserid = 2;

    // given
    List<PointHistory> currentPointHistories = new ArrayList<>();
    currentPointHistories.add(
        new PointHistory(1, userId, 100, TransactionType.CHARGE, System.currentTimeMillis()));
    currentPointHistories.add(
        new PointHistory(2, userId, 50, TransactionType.USE, System.currentTimeMillis()));
    currentPointHistories.add(
        new PointHistory(3, otherUserid, 100, TransactionType.CHARGE, System.currentTimeMillis()));

    pointHistoryTableStub.setTable(currentPointHistories);

    // when
    List<PointHistory> pointHistories = pointService.getPointHistories(userId);

    // then
    assertEquals(2, pointHistories.size());

    assertEquals(1, pointHistories.get(0).id());
    assertEquals(1, pointHistories.get(0).userId());
    assertEquals(100, pointHistories.get(0).amount());
    assertEquals(TransactionType.CHARGE, pointHistories.get(0).type());

    assertEquals(2, pointHistories.get(1).id());
    assertEquals(1, pointHistories.get(1).userId());
    assertEquals(50, pointHistories.get(1).amount());
    assertEquals(TransactionType.USE, pointHistories.get(1).type());
  }

  @Test
  public void getPointHistories_ByUserId_ButNothing() {
    long userId = 1;
    long otherUserid = 2;

    // given
    List<PointHistory> currentPointHistories = new ArrayList<>();
    currentPointHistories.add(
        new PointHistory(1, otherUserid, 100, TransactionType.CHARGE, System.currentTimeMillis()));

    pointHistoryTableStub.setTable(currentPointHistories);

    // when
    List<PointHistory> pointHistories = pointService.getPointHistories(userId);

    // then
    assertEquals(0, pointHistories.size());
  }

  @Test
  public void charge100Point_AtFirst_ThenHas100Point() {
    long userId = 1;

    // when
    UserPoint userPoint = pointService.charge(userId, 100);

    // then
    assertEquals(userId, userPoint.id());
    assertEquals(100, userPoint.point());
  }

  @Test
  public void charge100Point_ToExistUser_With100Point_ThenHas200Point() {
    long userId = 1;

    // given
    HashMap<Long, UserPoint> table = new HashMap<>();
    table.put(userId, new UserPoint(userId, 100, System.currentTimeMillis()));

    userPointTableStub.setTable(table);

    // when
    UserPoint userPoint = pointService.charge(userId, 100);

    // then
    assertEquals(userId, userPoint.id());
    assertEquals(200, userPoint.point());
  }

  @Test
  void chargeNegativePoint_ThenThrow_IllegalArgumentException() {
    long userId = 1;

    // when & then
    assertThrows(IllegalArgumentException.class, () -> pointService.charge(userId, -100));
  }

  @Test
  void use100Point_FromExistUser_With100Point_ThenHas0Point() {
    long userId = 1;

    // given
    HashMap<Long, UserPoint> table = new HashMap<>();
    table.put(userId, new UserPoint(userId, 100, System.currentTimeMillis()));

    userPointTableStub.setTable(table);

    // when
    UserPoint userPoint = pointService.use(userId, 100);

    // then
    assertEquals(userId, userPoint.id());
    assertEquals(0, userPoint.point());
  }

  @Test
  void useExceedPoint_ThenThrow_InsufficientPointsException() {
    long userId = 1;

    // given
    HashMap<Long, UserPoint> table = new HashMap<>();
    table.put(userId, new UserPoint(userId, 100, System.currentTimeMillis()));

    userPointTableStub.setTable(table);

    // when & then
    assertThrows(InsufficientPointsException.class, () -> pointService.use(userId, 200));
  }
}
