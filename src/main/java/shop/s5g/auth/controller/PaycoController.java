package shop.s5g.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.s5g.auth.dto.MessageDto;
import shop.s5g.auth.dto.TokenResponseDto;
import shop.s5g.auth.exception.AlreadyLinkAccountException;
import shop.s5g.auth.exception.MemberNotFoundException;
import shop.s5g.auth.service.PaycoService;
import shop.s5g.auth.service.TokenService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PaycoController {

    private final PaycoService paycoService;
    private final TokenService tokenService;

    @PostMapping("/payco")
    public ResponseEntity<TokenResponseDto> oauthIssueToken(@RequestParam(name = "code") String code){
        TokenResponseDto responseDto = paycoService.getToken(code);
        String paycoId = paycoService.getPaycoId(responseDto.accessToken());
        String loginId = paycoService.getMemberId(paycoId);

        TokenResponseDto tokenResponseDto = tokenService.issueToken(loginId, "ROLE_MEMBER");
        return ResponseEntity.ok().body(tokenResponseDto);
    }

    @PostMapping("/payco/link")
    public ResponseEntity<MessageDto> linkAccount(@RequestParam(name = "code") String code, @RequestHeader(name = HttpHeaders.AUTHORIZATION) String accessToken){
        TokenResponseDto responseDto = paycoService.getToken(code);
        String paycoId = paycoService.getPaycoId(responseDto.accessToken());
        String message = paycoService.linkAccount(paycoId, accessToken);
        return ResponseEntity.ok().body(new MessageDto(message));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<MessageDto> memberNotFound(MemberNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageDto(e.getMessage()));
    }

    @ExceptionHandler(AlreadyLinkAccountException.class)
    public ResponseEntity<MessageDto> alreadyLinkAccount(AlreadyLinkAccountException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageDto(e.getMessage()));
    }
}
