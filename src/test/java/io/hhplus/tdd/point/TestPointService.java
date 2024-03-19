package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TestPointService {
  @InjectMocks private PointServiceImpl pointService;
  @Mock private UserPointTable mockUserPointTable;
  @Mock private PointHistoryTable mockPointHistoryTable;

  @BeforeEach
  public void init() {
    MockitoAnnotations.initMocks(this);
    pointService = new PointServiceImpl(mockUserPointTable, mockPointHistoryTable);
  }

  @Test
  public void charge100Point_ToZeroPoint_ThenInsert100Point() {
    long userId = 1;
    long currentAmount = 0;
    long amount = 100;
    // given
    givenPointService(userId, currentAmount, amount, TransactionType.CHARGE);

    // when
    pointService.charge(userId, amount);

    // then
    Long capturedAmount = captureAmountArgument();
    assertEquals(amount, capturedAmount);
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
    assertEquals(0, capturedAmount);
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

    PointHistory pointHistory =
        new PointHistory(1, userId, amount, type, System.currentTimeMillis());

    when(mockPointHistoryTable.insert(userId, amount, type, System.currentTimeMillis()))
        .thenReturn(pointHistory);
  }

  /** table.insert argument 를 캡처 argument 가 updated 되어 있기를 기대하기 때문. */
  private Long captureAmountArgument() {
    ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
    then(mockUserPointTable).should().insertOrUpdate(captor.capture(), captor.capture());
    return captor.getValue();
  }
}
