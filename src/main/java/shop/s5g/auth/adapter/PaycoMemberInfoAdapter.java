package shop.s5g.auth.adapter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import shop.s5g.auth.dto.payco.PaycoMemberResponseDto;

@FeignClient(name = "payco-member", url = "${oauth.payco.member-info-url}")
public interface PaycoMemberInfoAdapter {

    @PostMapping
    ResponseEntity<PaycoMemberResponseDto> getMemberInfo(
        @RequestHeader(name = "client_id") String clientId,
        @RequestHeader(name = "access_token") String token);
}
