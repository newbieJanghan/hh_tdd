package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.exceptions.InsufficientPointsException;
import io.hhplus.tdd.point.model.UserPoint;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LockPointServiceImpl extends PointServiceImpl {
  private final ConcurrentHashMap<Long, Lock> lockMap = new ConcurrentHashMap<>();

  @Autowired
  public LockPointServiceImpl(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
    super(userPointTable, pointHistoryTable);
  }

  public UserPoint charge(long id, long amount, int countForTest) throws IllegalArgumentException {
    Lock lock = lockMap.computeIfAbsent(id, key -> new ReentrantLock(true));

    System.out.println("in charge, number " + countForTest + ", try to acquire lock");
    lock.lock();
    try {
      System.out.println("in charge, number " + countForTest + ", acquire lock!");
      return super.charge(id, amount);
    } finally {
      lock.unlock();
    }

    //    try {
    //      boolean acquired = lock.tryLock(5, TimeUnit.SECONDS);
    //      if (!acquired) {
    //        System.out.println("count: " + countForTest + " cannot acquire lock");
    //      }
    //    } catch (Exception e) {
    //      e.printStackTrace();
    //    }
    //
    //    try {
    //      return super.charge(id, amount);
    //    } finally {
    //      System.out.println("count: " + countForTest + " unlock");
    //      lock.unlock();
    //    }
  }

  public UserPoint use(long id, long amount, int countForTest)
      throws IllegalArgumentException, InsufficientPointsException {
    Lock lock = lockMap.computeIfAbsent(id, k -> new ReentrantLock(true));

    System.out.println("in use, number " + countForTest + ", try to acquire lock");
    lock.lock();
    try {
      System.out.println("in use, number " + countForTest + ", acquire lock!");
      return super.use(id, amount);
    } finally {
      lock.unlock();
    }
  }
}
