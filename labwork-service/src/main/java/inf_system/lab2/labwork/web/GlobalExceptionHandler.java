package inf_system.lab2.labwork.web;

import inf_system.lab2.labwork.dto.ErrorDto;
import inf_system.lab2.labwork.exception.BadRequestException;
import inf_system.lab2.labwork.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDto> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorDto> handleInvalidRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(HttpStatus.BAD_REQUEST.value(),
                        "Данные нарушают ограничения целостности"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Внутренняя ошибка сервера"));
    }
}
