package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import java.util.List;
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
  public void getUserPoint_ByUserId() {
    long userId = 1;
    long currentAmount = 100;
    long currentUpdateMillis = 100L;
    when(mockUserPointTable.selectById(userId))
        .thenReturn(new UserPoint(userId, currentAmount, currentUpdateMillis));

    UserPoint userPoint = pointService.getUserPoint(userId);
    assertEquals(userId, userPoint.id());
    assertEquals(currentAmount, userPoint.point());
    assertNotEquals(System.currentTimeMillis(), userPoint.updateMillis());
  }

  @Test
  public void getDefaultUserPoint_WhenUserPointIsNotExist() {
    long userId = 1;
    when(mockUserPointTable.selectById(userId)).thenReturn(UserPoint.empty(userId));

    UserPoint userPoint = pointService.getUserPoint(userId);
    assertEquals(userId, userPoint.id());
    assertEquals(0, userPoint.point());
  }

  // 이런 테스트도 유효한 건지.
  // 차리리 실 table 을 stubing 하는 게 나을 것 같은데,
  // 실제 DB layer 를 가정하고 모킹하다보니
  // 이런 테스트 방식이 유효한 건지 모르겠음.
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
