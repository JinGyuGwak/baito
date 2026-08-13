package com.baito.my_app.common.web;

import com.baito.my_app.assignment.domain.ShiftNotAvailableException;
import com.baito.my_app.assignment.domain.StaffQuotaExceededException;
import com.baito.my_app.common.exception.InvalidSlotTimeException;
import com.baito.my_app.group.domain.GroupNotFoundException;
import com.baito.my_app.group.domain.NotGroupOwnerException;
import com.baito.my_app.invitation.domain.DuplicatePendingInvitationException;
import com.baito.my_app.invitation.domain.InvalidInvitationStateException;
import com.baito.my_app.invitation.domain.InvalidInviteeException;
import com.baito.my_app.invitation.domain.InvitationAccessDeniedException;
import com.baito.my_app.invitation.domain.InvitationNotFoundException;
import com.baito.my_app.member.domain.DuplicateLoginIdException;
import com.baito.my_app.member.domain.MemberNotFoundException;
import com.baito.my_app.membership.domain.NotGroupMemberException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---- 400 Bad Request ----

    @ExceptionHandler({InvalidInviteeException.class, InvalidSlotTimeException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, code(ex), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("不正なリクエストです。");
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    // ---- 401 / 403 ----

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "ログインIDまたはパスワードが正しくありません。");
    }

    @ExceptionHandler({
            NotGroupOwnerException.class,
            NotGroupMemberException.class,
            InvitationAccessDeniedException.class,
            AccessDeniedException.class
    })
    public ResponseEntity<ErrorResponse> handleForbidden(RuntimeException ex) {
        String message = (ex instanceof AccessDeniedException) ? "アクセス権限がありません。" : ex.getMessage();
        return build(HttpStatus.FORBIDDEN, code(ex), message);
    }

    // ---- 404 Not Found ----

    @ExceptionHandler({
            MemberNotFoundException.class,
            GroupNotFoundException.class,
            InvitationNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return build(HttpStatus.NOT_FOUND, code(ex), ex.getMessage());
    }

    // ---- 409 Conflict (includes the assignment alerts of requirement 7) ----

    @ExceptionHandler({
            DuplicateLoginIdException.class,
            DuplicatePendingInvitationException.class,
            InvalidInvitationStateException.class,
            ShiftNotAvailableException.class,
            StaffQuotaExceededException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        return build(HttpStatus.CONFLICT, code(ex), ex.getMessage());
    }

    // ---- 404 for unmapped routes / 500 fallback ----

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "リクエストされたリソースが見つかりません。");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "サーバーエラーが発生しました。");
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message));
    }

    /** Uses the exception's simple class name (minus the "Exception" suffix) as a SCREAMING_SNAKE code. */
    private static String code(Throwable ex) {
        String simple = ex.getClass().getSimpleName().replaceAll("Exception$", "");
        return simple.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase();
    }
}
