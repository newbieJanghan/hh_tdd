package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.exceptions.InsufficientPointsException;
import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.TransactionType;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.service.PointServiceImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TestPointServiceWithMock {
  @InjectMocks private PointServiceImpl pointService;
  @Mock private UserPointTable mockUserPointTable;
  @Mock private PointHistoryTable mockPointHistoryTable;

  @BeforeEach
  public void init() {
    MockitoAnnotations.initMocks(this);
    pointService = new PointServiceImpl(mockUserPointTable, mockPointHistoryTable);
  }

  @Test
  public void getUserPoint_ByUserId() {
    long userId = 1;
    long currentAmount = 100;
    long currentUpdateMillis = 100L;
    UserPoint currentUserPoint = new UserPoint(userId, currentAmount, currentUpdateMillis);
    when(mockUserPointTable.selectById(userId)).thenReturn(currentUserPoint);

    UserPoint userPoint = pointService.getUserPoint(userId);
    assertEquals(userId, userPoint.id());
    assertEquals(currentAmount, userPoint.point());
    assertNotEquals(System.currentTimeMillis(), userPoint.updateMillis());
  }

  @Test
  public void getDefaultUserPoint_WhenUserPointIsNotExist() {
    long userId = 1;
    UserPoint defaultUserPoint = UserPoint.empty(userId);
    when(mockUserPointTable.selectById(userId)).thenReturn(defaultUserPoint);

    UserPoint userPoint = pointService.getUserPoint(userId);
    assertEquals(defaultUserPoint.id(), userPoint.id());
    assertEquals(defaultUserPoint.point(), userPoint.point());
    assertEquals(defaultUserPoint.updateMillis(), userPoint.updateMillis());
  }

  @Test
  public void getPointHistories_ByUserId() {
    long userId = 1;
    PointHistory[] currentPointHistories =
        new PointHistory[] {
          new PointHistory(1, userId, 100, TransactionType.CHARGE, System.currentTimeMillis()),
          new PointHistory(2, userId, 50, TransactionType.USE, System.currentTimeMillis())
        };

    when(mockPointHistoryTable.selectAllByUserId(userId))
        .thenReturn(List.of(currentPointHistories));

    List<PointHistory> pointHistories = pointService.getPointHistories(userId);
    assertEquals(2, pointHistories.size());
  }

  @Test
  public void charge100Point_ToZeroPoint_ThenInsert100Point() throws InterruptedException {
    long userId = 1;
    long currentAmount = 0;
    long amount = 100;
    // given
    givenPointService(userId, currentAmount, amount, TransactionType.CHARGE);

    // when
    pointService.charge(userId, amount);

    // then
    Long capturedAmount = captureAmountArgument();
    assertEquals((currentAmount + amount), capturedAmount);
  }

  @Test
  void chargeNegativePoint_ThenThrow_IllegalArgumentException() {
    long userId = 1;
    long currentAmount = 0;
    long amount = -100;

    // given
    givenPointService(userId, currentAmount, amount, TransactionType.CHARGE);

    // then
    assertThrows(IllegalArgumentException.class, () -> pointService.charge(userId, amount));
  }

  @Test
  void use100Point_From100Point_ThenInsert0Point() {
    long userId = 1;
    long currentAmount = 100;
    long amount = 100;
    // given
    givenPointService(userId, currentAmount, amount, TransactionType.USE);

    // when
    pointService.use(userId, amount);

    // then
    Long capturedAmount = captureAmountArgument();
    assertEquals((currentAmount - amount), capturedAmount);
  }

  @Test
  void useExceedPoint_ThenThrow_InsufficientPointException() {
    long userId = 1;
    long currentAmount = 50;
    long amount = 100;
    // given
    givenPointService(userId, currentAmount, amount, TransactionType.USE);

    // then
    assertThrows(InsufficientPointsException.class, () -> pointService.use(userId, amount));
  }

  private void givenPointService(
      long userId, long currentAmount, long amount, TransactionType type) {
    when(mockUserPointTable.selectById(userId))
        .thenReturn(new UserPoint(userId, currentAmount, System.currentTimeMillis()));

    when(mockPointHistoryTable.insert(anyLong(), anyLong(), any(TransactionType.class), anyLong()))
        .thenReturn(new PointHistory(1, userId, amount, type, System.currentTimeMillis()));
  }

  // table.insert argument 를 캡처하는 세팅입니다.
  // service 입장에선 argument 가 updated 되어 있기를 기대하기 때문에
  // 이를 캡쳐하여 확인합니다.
  private Long captureAmountArgument() {
    ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
    then(mockUserPointTable).should().insertOrUpdate(captor.capture(), captor.capture());
    return captor.getValue();
  }
}
