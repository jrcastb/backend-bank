package com.devsu.backendbank.infrastructure.exception;

import com.devsu.backendbank.infrastructure.exception.message.BusinessErrorMessage;
import com.devsu.backendbank.infrastructure.exception.message.ErrorModel;
import com.devsu.backendbank.infrastructure.exception.message.TechnicalErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorModel> handleBusinessException(BusinessException ex,
                                                              HttpServletRequest request) {
        HttpStatus status = mapBusinessStatus(ex.getBusinessErrorMessage());
        log.warn("Business exception [{}] at {}: {}", ex.getBusinessErrorMessage().name(),
                getCurrentEndpointPath(request), ex.getMessage());
        return ResponseEntity.status(status).body(buildBusinessError(ex, status, request));
    }

    @ExceptionHandler(TechnicalException.class)
    public ResponseEntity<ErrorModel> handleTechnicalException(TechnicalException ex,
                                                               HttpServletRequest request) {
        HttpStatus status = mapTechnicalStatus(ex.getTechnicalErrorMessage());
        log.error("Technical exception [{}] at {}", ex.getTechnicalErrorMessage().name(),
                getCurrentEndpointPath(request), ex);
        return ResponseEntity.status(status).body(buildTechnicalError(ex, status, request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorModel> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                    HttpServletRequest request) {
        List<ErrorModel.FieldErrorItem> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        ErrorModel response = baseError(HttpStatus.BAD_REQUEST,
                BusinessErrorMessage.BAD_REQUEST_BODY.name(),
                BusinessErrorMessage.BAD_REQUEST_BODY.getInstance(),
                BusinessErrorMessage.BAD_REQUEST_BODY.getDetail(),
                request)
                .fieldErrors(fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorModel> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                    HttpServletRequest request) {
        log.warn("Malformed JSON request at {}", getCurrentEndpointPath(request), ex);
        return ResponseEntity.badRequest().body(baseError(HttpStatus.BAD_REQUEST,
                TechnicalErrorMessage.INVALID_REQUEST_FORMAT.name(),
                TechnicalErrorMessage.INVALID_REQUEST_FORMAT.getInstance(),
                TechnicalErrorMessage.INVALID_REQUEST_FORMAT.getDetail(),
                request));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorModel> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest request) {
        log.warn("HTTP method not supported at {}", getCurrentEndpointPath(request), ex);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(baseError(HttpStatus.METHOD_NOT_ALLOWED,
                        TechnicalErrorMessage.METHOD_NOT_ALLOWED.name(),
                        TechnicalErrorMessage.METHOD_NOT_ALLOWED.getInstance(),
                        TechnicalErrorMessage.METHOD_NOT_ALLOWED.getDetail(),
                        request));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorModel> handleConstraintViolation(ConstraintViolationException ex,
                                                                HttpServletRequest request) {
        List<ErrorModel.FieldErrorItem> fieldErrors = ex.getConstraintViolations().stream()
                .map(violation -> new ErrorModel.FieldErrorItem()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage()))
                .toList();
        return ResponseEntity.badRequest().body(baseError(HttpStatus.BAD_REQUEST,
                        TechnicalErrorMessage.METHOD_ARGUMENT_NOT_VALID.name(),
                        TechnicalErrorMessage.METHOD_ARGUMENT_NOT_VALID.getInstance(),
                        TechnicalErrorMessage.METHOD_ARGUMENT_NOT_VALID.getDetail(),
                        request)
                .fieldErrors(fieldErrors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorModel> handleExceptions(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}", getCurrentEndpointPath(request), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(baseError(HttpStatus.INTERNAL_SERVER_ERROR,
                        TechnicalErrorMessage.INTERNAL_ERROR.name(),
                        TechnicalErrorMessage.INTERNAL_ERROR.getInstance(),
                        TechnicalErrorMessage.INTERNAL_ERROR.getDetail(),
                        request));
    }

    private ErrorModel buildBusinessError(BusinessException ex, HttpStatus status, HttpServletRequest request) {
        String detail = resolveBusinessDetail(ex);
        BusinessErrorMessage message = ex.getBusinessErrorMessage();
        return baseError(status, message.name(), message.getInstance(), detail, request);
    }

    private ErrorModel buildTechnicalError(TechnicalException ex, HttpStatus status, HttpServletRequest request) {
        TechnicalErrorMessage message = ex.getTechnicalErrorMessage();
        return baseError(status, message.name(), message.getInstance(), message.getDetail(), request);
    }

    private ErrorModel baseError(HttpStatus status,
                                 String title,
                                 String code,
                                 String message,
                                 HttpServletRequest request) {
        return new ErrorModel()
                .timestamp(ISO_FORMATTER.format(OffsetDateTime.now(ZoneOffset.UTC)))
                .status(status.value())
                .title(title)
                .code(code)
                .instance(code)
                .message(message)
                .detail(message)
                .path(getCurrentEndpointPath(request))
                .traceId(resolveTraceId(request))
                .type(status.getReasonPhrase());
    }

    private HttpStatus mapBusinessStatus(BusinessErrorMessage message) {
        return switch (message) {
            case CLIENT_NOT_FOUND, ACCOUNT_NOT_FOUND, NO_MOVEMENTS_FOUND -> HttpStatus.NOT_FOUND;
            case ACCOUNT_ALREADY_EXISTS, CLIENT_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case INSUFFICIENT_FUNDS, DAILY_LIMIT_EXCEEDED -> HttpStatus.UNPROCESSABLE_CONTENT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private HttpStatus mapTechnicalStatus(TechnicalErrorMessage message) {
        return switch (message) {
            case INVALID_REQUEST_FORMAT, METHOD_ARGUMENT_NOT_VALID, ILLEGAL_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case SERVICE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case METHOD_NOT_ALLOWED -> HttpStatus.METHOD_NOT_ALLOWED;
            case RESOURCE_LOCKED -> HttpStatus.CONFLICT;
            case TIMEOUT_ERROR -> HttpStatus.REQUEST_TIMEOUT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private ErrorModel.FieldErrorItem toFieldError(FieldError fieldError) {
        String field = fieldError.getField();
        String message = fieldError.getDefaultMessage() != null
                ? fieldError.getDefaultMessage()
                : "Invalid value";
        return new ErrorModel.FieldErrorItem().field(field).message(message);
    }

    private String resolveBusinessDetail(BusinessException ex) {
        String detailTemplate = ex.getBusinessErrorMessage().getDetail();
        if (detailTemplate.contains("%s") && ex.getExtra() != null && !ex.getExtra().isBlank()) {
            return String.format(detailTemplate, ex.getExtra());
        }
        return detailTemplate;
    }

    private static String getCurrentEndpointPath(HttpServletRequest request) {
        return (request != null && request.getRequestURI() != null)
                ? request.getRequestURI()
                : "unknown";
    }

    private String resolveTraceId(HttpServletRequest request) {
        String fromHeader = request != null ? request.getHeader("X-Request-Id") : null;
        if (fromHeader != null && !fromHeader.isBlank()) {
            return fromHeader;
        }
        String fromContext = ThreadContext.get("traceId");
        return (fromContext != null && !fromContext.isBlank()) ? fromContext : "n/a";
    }

}
