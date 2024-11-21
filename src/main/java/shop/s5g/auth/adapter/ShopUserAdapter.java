package shop.s5g.auth.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import shop.s5g.auth.dto.LoginResponseDto;
import shop.s5g.auth.dto.MemberLoginIdResponseDto;
import shop.s5g.auth.dto.MessageDto;

@FeignClient(value = "shop-service")
public interface ShopUserAdapter {

    @GetMapping("/api/shop/member/login/{loginId}")
    ResponseEntity<LoginResponseDto> getUserInfo(@PathVariable String loginId);

    @GetMapping("/api/shop/admin/login/{loginId}")
    ResponseEntity<LoginResponseDto> getAdminInfo(@PathVariable String loginId);

    @PutMapping("/api/shop/member/{loginId}")
    ResponseEntity<Void> updateLatestLoginAt(@PathVariable String loginId);

    @GetMapping("/api/shop/member/payco")
    ResponseEntity<MemberLoginIdResponseDto> getMemberIdByPaycoId(
        @RequestParam(name = "payco_id") String paycoId);

    @PostMapping("/api/shop/member/payco/link")
    ResponseEntity<MessageDto> linkAccount(@RequestParam(name = "payco_id") String paycoId,
        @RequestHeader(name = HttpHeaders.AUTHORIZATION) String accessToken);
}
