package com.ikhsan.securepaywallet.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {
        GlobalExceptionHandler.class,
        GlobalExceptionHandlerWebMvcTest.TestErrorController.class
})
class GlobalExceptionHandlerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void handleResponseStatusException_shouldReturnMappedStatusAndReason() throws Exception {
        mockMvc.perform(get("/test-errors/response-status"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").value("conflict occurred"));
    }

    @Test
    void handleMethodArgumentNotValidException_shouldReturnBadRequestWithFieldErrors() throws Exception {
        mockMvc.perform(post("/test-errors/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").value("title: title must not be blank"));
    }

    @Test
    void handleConstraintViolationException_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test-errors/constraint-violation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").value("constraint violation"));
    }

    @Test
    void handleIllegalArgumentException_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/test-errors/illegal-argument"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").value("session not found"));
    }

    @Test
    void handleUsernameNotFoundException_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/test-errors/user-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").value("user not found"));
    }

    @Test
    void handleAccessDeniedException_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/test-errors/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").value("Access denied"));
    }

    @Test
    void handleGenericException_shouldReturnInternalServerErrorWithoutStackTrace() throws Exception {
        mockMvc.perform(get("/test-errors/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").value("Internal server error"));
    }

    @RestController
    static class TestErrorController {

        @GetMapping("/test-errors/response-status")
        public void throwResponseStatus() {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "conflict occurred");
        }

        @PostMapping("/test-errors/validation")
        public void throwValidation(@Valid @RequestBody SampleRequest request) {
        }

        @GetMapping("/test-errors/constraint-violation")
        public void throwConstraintViolation() {
            throw new ConstraintViolationException("constraint violation", Collections.emptySet());
        }

        @GetMapping("/test-errors/illegal-argument")
        public void throwIllegalArgument() {
            throw new IllegalArgumentException("session not found");
        }

        @GetMapping("/test-errors/user-not-found")
        public void throwUsernameNotFound() {
            throw new UsernameNotFoundException("user not found");
        }

        @GetMapping("/test-errors/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException("Access denied");
        }

        @GetMapping("/test-errors/generic")
        public void throwGeneric() {
            throw new RuntimeException("fatal internal root cause");
        }
    }

    @Getter
    @Setter
    static class SampleRequest {

        @NotBlank(message = "title must not be blank")
        private String title;
    }
}
