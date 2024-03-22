package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.hhplus.tdd.database.PointHistoryTableStub;
import io.hhplus.tdd.database.UserPointTableStub;
import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.service.LockPointServiceImpl;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestLockPointServiceWithStub {
  private UserPointTableStub userPointTableStub;
  private PointHistoryTableStub pointHistoryTableStub;
  private LockPointServiceImpl lockPointService;

  @BeforeEach
  public void setUp() {
    userPointTableStub = new UserPointTableStub();
    pointHistoryTableStub = new PointHistoryTableStub();
    lockPointService = new LockPointServiceImpl(userPointTableStub, pointHistoryTableStub);
  }

  @Test
  public void charge100PointConcurrently_ToExistUser_With0Point() throws InterruptedException {
    long userId = 1;
    long currentPointAmount = 0;
    UserPoint currentUserPoint =
        new UserPoint(userId, currentPointAmount, System.currentTimeMillis());
    long chargeAmount = 100;

    // given
    HashMap<Long, UserPoint> table = new HashMap<>();
    table.put(userId, currentUserPoint);

    userPointTableStub.setTable(table);

    int threadCount = 10;

    CountDownLatch latch = new CountDownLatch(threadCount);

    // when
    for (int i = 0; i < threadCount; i++) {
      Thread.sleep(10);
      int count = i;
      Runnable task =
          () -> {
            try {
              lockPointService.charge(userId, chargeAmount, count);
            } catch (Exception e) {
              System.out.println(e);
            } finally {
              latch.countDown();
            }
          };
      new Thread(task).start();
    }
    latch.await();

    // then
    UserPoint userPoint = userPointTableStub.selectById(userId);
    assertEquals(currentPointAmount + (chargeAmount * threadCount), userPoint.point());

    List<PointHistory> pointHistories = pointHistoryTableStub.selectAllByUserId(userId);
    assertEquals(threadCount, pointHistories.size());
  }

  @Test
  public void use100PointConcurrently_ToExistUser_With1000Point() throws InterruptedException {
    long userId = 1;
    long currentPointAmount = 1000;
    UserPoint currentUserPoint =
        new UserPoint(userId, currentPointAmount, System.currentTimeMillis());
    long useAmount = 100;

    // given
    HashMap<Long, UserPoint> table = new HashMap<>();
    table.put(userId, currentUserPoint);

    userPointTableStub.setTable(table);

    int threadCount = 10;

    CountDownLatch latch = new CountDownLatch(threadCount);

    // when
    for (int i = 0; i < threadCount; i++) {
      Thread.sleep(10);
      int count = i;
      Runnable task =
          () -> {
            try {
              lockPointService.use(userId, useAmount, count);
            } catch (Exception e) {
              System.out.println(e);
            } finally {
              latch.countDown();
            }
          };
      new Thread(task).start();
    }
    latch.await();

    // then
    UserPoint userPoint = userPointTableStub.selectById(userId);
    assertEquals(currentPointAmount - (useAmount * threadCount), userPoint.point());

    List<PointHistory> pointHistories = pointHistoryTableStub.selectAllByUserId(userId);
    assertEquals(threadCount, pointHistories.size());
  }

  @Test
  public void
      charge100Point_ThenUse200PointConcurrently_ToExistUser_With1000Point_UntilPointIsOver()
          throws InterruptedException {
    long userId = 1;
    long currentPointAmount = 1000;
    UserPoint currentUserPoint =
        new UserPoint(userId, currentPointAmount, System.currentTimeMillis());
    long chargeAmount = 100;
    long useAmount = 200;
    long decreaseAmountPerIteration = useAmount - chargeAmount;
    int iterationCount = 10;

    // given
    HashMap<Long, UserPoint> table = new HashMap<>();
    table.put(userId, currentUserPoint);

    userPointTableStub.setTable(table);

    int threadCount = iterationCount * 2;

    CountDownLatch latch = new CountDownLatch(threadCount);

    // when
    for (int i = 0; i < threadCount; i++) {
      System.out.println();
      Thread.sleep(100);
      int count = i;
      Runnable task =
          () -> {
            try {
              if (count % 2 == 0) {
                lockPointService.charge(userId, chargeAmount, count);
              } else {
                lockPointService.use(userId, useAmount, count);
              }
            } catch (Exception e) {
              System.out.println(e);
            } finally {
              latch.countDown();
              System.out.println("countdown " + latch.getCount());
            }
          };

      new Thread(task).start();
    }
    latch.await();

    // then
    UserPoint userPoint = userPointTableStub.selectById(userId);
    assertEquals(
        currentPointAmount - (decreaseAmountPerIteration * iterationCount), userPoint.point());

    List<PointHistory> pointHistories = pointHistoryTableStub.selectAllByUserId(userId);
    assertEquals(threadCount, pointHistories.size());
  }
}
