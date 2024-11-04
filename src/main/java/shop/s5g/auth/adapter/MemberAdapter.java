package shop.s5g.auth.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shop.s5g.auth.dto.LoginResponseDto;

@FeignClient(name = "member-service", url = "http://localhost:8820")
public interface MemberAdapter {

    @GetMapping("/api/shop/member/login/{loginId}")
    ResponseEntity<LoginResponseDto> getUserInfo(@PathVariable String loginId);
}
