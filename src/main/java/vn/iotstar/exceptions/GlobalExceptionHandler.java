package vn.iotstar.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;

/**
 * Bảng của slide 30, nhưng JWT sai/hết hạn trả 401 (slide ghi 401 trong bảng nhưng code lại trả 403).
 * Spring chọn handler theo độ gần của kiểu exception: ExpiredJwtException / SignatureException
 * (con của JwtException) được xử lý trước handler JwtException chung.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ProblemDetail problem(HttpStatus status, String message, String description) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, message);
        pd.setProperty("description", description);
        return pd;
    }

    // Thông tin đăng nhập không hợp lệ: BadCredentialsException (con của AuthenticationException) -> 401
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return problem(HttpStatus.UNAUTHORIZED, ex.getMessage(), "The username or password is incorrect");
    }

    // Tài khoản bị khóa: AccountStatusException -> 403
    @ExceptionHandler(AccountStatusException.class)
    public ProblemDetail handleAccountStatus(AccountStatusException ex) {
        return problem(HttpStatus.FORBIDDEN, ex.getMessage(), "The account is locked");
    }

    // Không được phép truy cập tài nguyên: AccessDeniedException -> 403
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, ex.getMessage(), "You are not authorized to access this resource");
    }

    // JWT không hợp lệ: SignatureException -> 401
    @ExceptionHandler(SignatureException.class)
    public ProblemDetail handleSignature(SignatureException ex) {
        return problem(HttpStatus.UNAUTHORIZED, ex.getMessage(), "The JWT signature is invalid");
    }

    // JWT đã hết hạn: ExpiredJwtException -> 401
    @ExceptionHandler(ExpiredJwtException.class)
    public ProblemDetail handleExpired(ExpiredJwtException ex) {
        return problem(HttpStatus.UNAUTHORIZED, ex.getMessage(), "The JWT token has expired");
    }

    // Token rác (MalformedJwtException), thiếu/sai issuer, thuật toán không hỗ trợ...: lỗi của client -> 401, không phải 500
    @ExceptionHandler(JwtException.class)
    public ProblemDetail handleJwt(JwtException ex) {
        return problem(HttpStatus.UNAUTHORIZED, ex.getMessage(), "The JWT is invalid");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleConflict(DataIntegrityViolationException ex) {
        return problem(HttpStatus.CONFLICT, "Conflict", "Email already exists or data violates a constraint");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleOthers(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unknown internal server error.");
    }
}
