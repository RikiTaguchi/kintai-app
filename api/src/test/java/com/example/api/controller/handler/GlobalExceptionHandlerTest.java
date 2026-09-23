package com.example.api.controller.handler;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.exception.AlreadyExistsException;
import com.example.api.exception.AuthorizationFailedException;
import com.example.api.exception.BusinessException;
import com.example.api.exception.InvalidInputException;
import com.example.api.exception.LoginException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.service.dto.ManagerDto;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @Mock private DummyService dummyService;

    @BeforeEach
    void setUp() {
        DummyController controller = new DummyController(dummyService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    // --- 各例外 → HTTP Status マッピング ---

    @Test
    @DisplayName("AlreadyExistsException → 409")
    void alreadyExists() throws Exception {
        when(dummyService.trigger(anyUuid()))
            .thenThrow(new AlreadyExistsException("重複しています"));

        mockMvc.perform(get("/test").param("q", "alreadyExists")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("重複しています"));
    }

    @Test
    @DisplayName("BusinessException → 422")
    void business() throws Exception {
        when(dummyService.trigger(anyUuid()))
            .thenThrow(new BusinessException("ルール違反"));

        mockMvc.perform(get("/test").param("q", "business"))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.error").value("ルール違反"));
    }

    @Test
    @DisplayName("InvalidInputException → 400")
    void invalidInput() throws Exception {
        when(dummyService.trigger(anyUuid()))
            .thenThrow(new InvalidInputException("入力不正"));

        mockMvc.perform(get("/test").param("q", "invalidInput"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("入力不正"));
    }

    @Test
    @DisplayName("LoginException → 401")
    void login() throws Exception {
        when(dummyService.trigger(anyUuid()))
            .thenThrow(new LoginException("認証失敗"));

        mockMvc.perform(get("/test").param("q", "login"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("認証失敗"));
    }

    @Test
    @DisplayName("AuthorizationFailedException → 403")
    void authorizationFailed() throws Exception {
        when(dummyService.trigger(anyUuid()))
            .thenThrow(new AuthorizationFailedException("権限なし"));

        mockMvc.perform(get("/test").param("q", "authorizationFailed"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("権限なし"));
    }

    @Test
    @DisplayName("ResourceNotFoundException → 404")
    void resourceNotFound() throws Exception {
        when(dummyService.trigger(anyUuid()))
            .thenThrow(new ResourceNotFoundException("見つからない"));

        mockMvc.perform(get("/test").param("q", "resourceNotFound"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("見つからない"));
    }

    @Test
    @DisplayName("未知の Exception → 500 + 汎用メッセージ")
    void unknown() throws Exception {
        when(dummyService.trigger(anyUuid()))
            .thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(get("/test").param("q", "unknown"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("予期せぬエラーが発生しました"));
    }

    // === MethodArgumentNotValidException → 400（Bean Validation）===

    @Test
    @DisplayName("MethodArgumentNotValidException（単一フィールド） → 400 + `field: message` 形式")
    void methodArgumentNotValid_singleField() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "tutorId", "Tutor id is required"));

        MethodArgumentNotValidException ex =
            new MethodArgumentNotValidException(mockExecutable(), bindingResult);

        mockMvc = MockMvcBuilders
            .standaloneSetup(new ThrowingValidationController(ex))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        mockMvc.perform(get("/throw-validation").param("q", "validation"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("tutorId: Tutor id is required"));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException（複数フィールド） → カンマ区切り連結")
    void methodArgumentNotValid_multipleFields() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "tutorId", "Tutor id is required"));
        bindingResult.addError(new FieldError("request", "workingDate", "Working date is required"));

        MethodArgumentNotValidException ex =
            new MethodArgumentNotValidException(mockExecutable(), bindingResult);

        mockMvc = MockMvcBuilders
            .standaloneSetup(new ThrowingValidationController(ex))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        mockMvc.perform(get("/throw-validation").param("q", "validation"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "tutorId: Tutor id is required, workingDate: Working date is required"));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException（ネストしたフィールド） → ネストパスが表示される")
    void methodArgumentNotValid_nestedField() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "lessonWorkDetail.periodCodes",
            "Period codes must not be null"));

        MethodArgumentNotValidException ex =
            new MethodArgumentNotValidException(mockExecutable(), bindingResult);

        mockMvc = MockMvcBuilders
            .standaloneSetup(new ThrowingValidationController(ex))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        mockMvc.perform(get("/throw-validation").param("q", "validation"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "lessonWorkDetail.periodCodes: Period codes must not be null"));
    }

    private MethodParameter mockExecutable() throws NoSuchMethodException {
        java.lang.reflect.Method m =
            DummyController.class.getMethod("test");
        return new MethodParameter(m, -1);
    }

    /**
     * 特定例外を無条件で投げるテスト用コントローラ。
     * Mockito の thenThrow では checked exception をメソッドシグネチャに
     * 宣言のないメソッドへ直接設定できないので、専用 controller で代替する。
     */
    @RestController
    static class ThrowingValidationController {

        private final MethodArgumentNotValidException ex;

        ThrowingValidationController(MethodArgumentNotValidException ex) {
            this.ex = ex;
        }

        @GetMapping("/throw-validation")
        public ManagerDto throwValidation() throws MethodArgumentNotValidException {
            throw ex;
        }
    }

    // === テスト用コントローラ ===

    @RestController
    static class DummyController {

        private final DummyService service;

        DummyController(DummyService service) {
            this.service = service;
        }

        @GetMapping("/test")
        public ManagerDto test() {
            return service.trigger(UUID.randomUUID());
        }
    }

    interface DummyService {
        ManagerDto trigger(UUID id);
    }

    private UUID anyUuid() {
        return org.mockito.ArgumentMatchers.any();
    }
}
