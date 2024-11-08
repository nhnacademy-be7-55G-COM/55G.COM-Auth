package shop.s5g.auth.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import shop.s5g.auth.dto.LoginResponseDto;

@FeignClient(value = "shop-service")
public interface MemberAdapter {

    @GetMapping("/api/shop/member/login/{loginId}")
    ResponseEntity<LoginResponseDto> getUserInfo(@PathVariable String loginId);

    @PutMapping("/api/shop/member/{loginId}")
    ResponseEntity<Void> updateLatestLoginAt(@PathVariable String loginId);
}
