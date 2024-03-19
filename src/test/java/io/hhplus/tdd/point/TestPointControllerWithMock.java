package io.hhplus.tdd.point;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// 컨트롤러에게 기대하는 행동은
// 1. 라우팅 2. 파라미터 바인딩 3. 서비스 호출 4. 응답 생성 및 반환이다.
// dto validation, authentication 등도 할 수 있지만, 결국 다른 레이어의 힘을 빌리는 것이므로 모킹 대상이다.
// 아래 테스트에서는 컨트롤러의 행동 전반을 테스트했다.
//
// 에러를 서비스 레이어에서 던지지만, 해당 에러가 리스폰스로 잘 들어오는지 확인해보니
// APiControllerAdvice 의 exception handler 가 무조건 500 에러로 던지는 것을 수정할 수 있었다.
@WebMvcTest(PointController.class)
public class TestPointControllerWithMock {
  @Autowired private MockMvc mockMvc;

  @MockBean private PointServiceImpl pointService;

  @Test
  public void getUserPoint() throws Exception {
    when(pointService.getUserPoint(1)).thenReturn(new UserPoint(1, 1000, 100L));

    mockMvc
        .perform(get("/point/1"))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"id\":1,\"point\":1000,\"updateMillis\":100}"));
  }

  @Test
  public void getPointHistories() throws Exception {
    when(pointService.getPointHistories(1))
        .thenReturn(List.of(new PointHistory(1, 1, 100, TransactionType.CHARGE, 100L)));

    mockMvc
        .perform(get("/point/1/histories"))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .json(
                    "[{\"id\":1,\"userId\":1,\"amount\":100,\"type\":\"CHARGE\",\"updateMillis\":100}]"));
  }

  @Test
  public void chargePoint() throws Exception {
    when(pointService.charge(1, 100)).thenReturn(new UserPoint(1, 1100, 100L));

    mockMvc
        .perform(patch("/point/1/charge").contentType(MediaType.APPLICATION_JSON).content("100"))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"id\":1,\"point\":1100,\"updateMillis\":100}"));
  }

  @Test
  public void chargeInvalidPoint_ThenThrowBadRequestError() throws Exception {
    when(pointService.charge(1, -100))
        .thenThrow(new IllegalArgumentException("amount should be positive"));

    mockMvc
        .perform(patch("/point/1/charge").contentType(MediaType.APPLICATION_JSON).content("-100"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("amount should be positive"));
  }

  @Test
  public void usePoint() throws Exception {
    when(pointService.use(1, 100)).thenReturn(new UserPoint(1, 900, 100L));

    mockMvc
        .perform(patch("/point/1/use").contentType(MediaType.APPLICATION_JSON).content("100"))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"id\":1,\"point\":900,\"updateMillis\":100}"));
  }

  @Test
  public void usePoint_ThenThrowInsufficientPointsError() throws Exception {
    when(pointService.use(1, 100000)).thenThrow(new InsufficientPointsException());

    mockMvc
        .perform(patch("/point/1/use").contentType(MediaType.APPLICATION_JSON).content("100000"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Insufficient points"));
  }
}
