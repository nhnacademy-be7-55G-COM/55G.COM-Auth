package shop.s5g.auth.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shop.s5g.auth.dto.MessageDto;
import shop.s5g.auth.exception.InvalidResponseException;

@RestControllerAdvice
public class WebRestAdvice {

    @ExceptionHandler(InvalidResponseException.class)
    public ResponseEntity<MessageDto> handleInvalidResponseException(InvalidResponseException e) {
        return ResponseEntity.badRequest().body(new MessageDto("옳지 못한 인증 요청입니다."));
    }
}
