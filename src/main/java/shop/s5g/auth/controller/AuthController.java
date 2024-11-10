package shop.s5g.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.s5g.auth.dto.TokenResponseDto;
import shop.s5g.auth.dto.UserDetailResponseDto;
import shop.s5g.auth.service.TokenService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenService tokenService;

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDto> reissueToken(@RequestHeader(name = "Authorization") String authorizationHeader){
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        String refreshToken = authorizationHeader.substring(7);
        TokenResponseDto tokenResponseDto = tokenService.reissueToken(refreshToken);
        return ResponseEntity.ok().body(tokenResponseDto);
    }

    @GetMapping("/id/{uuid}")
    public ResponseEntity<UserDetailResponseDto> getUserDetail(@PathVariable String uuid) {

        return ResponseEntity.ok().body(tokenService.getUserByUUID(uuid));
    }
}
