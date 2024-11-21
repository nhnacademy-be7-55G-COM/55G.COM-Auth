package shop.s5g.auth.service;

import feign.FeignException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import shop.s5g.auth.adapter.PaycoMemberInfoAdapter;
import shop.s5g.auth.adapter.PaycoTokenAdapter;
import shop.s5g.auth.adapter.ShopUserAdapter;
import shop.s5g.auth.dto.MemberLoginIdResponseDto;
import shop.s5g.auth.dto.MessageDto;
import shop.s5g.auth.dto.TokenResponseDto;
import shop.s5g.auth.dto.payco.PaycoMemberResponseDto;
import shop.s5g.auth.dto.payco.PaycoResponseDto;
import shop.s5g.auth.exception.AlreadyLinkAccountException;
import shop.s5g.auth.exception.MemberNotFoundException;
import shop.s5g.auth.exception.PaycoGetMemberInfoFailedException;
import shop.s5g.auth.exception.PaycoGetTokenFailedException;
import shop.s5g.auth.exception.PaycoLinkAccountFailedException;

@Service
@RequiredArgsConstructor
public class PaycoService {

    private final PaycoTokenAdapter paycoTokenAdapter;
    private final PaycoMemberInfoAdapter paycoMemberInfoAdapter;
    private final ShopUserAdapter shopUserAdapter;

    @Value("${oauth.payco.client-id}")
    private String clientId;

    @Value("${oauth.payco.client-secret}")
    private String clientSecret;

    public TokenResponseDto getToken(String code) {
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);

        try {
            ResponseEntity<PaycoResponseDto> response = paycoTokenAdapter.getToken(params);
            if (response.getStatusCode().is2xxSuccessful()) {
                PaycoResponseDto responseDto = response.getBody();

                if (responseDto != null) {
                    return new TokenResponseDto(responseDto.accessToken(),
                        responseDto.refreshToken());
                }
            }
            throw new PaycoGetTokenFailedException("payco token error");
        } catch (FeignException e) {
            throw new PaycoGetTokenFailedException("payco get token error");
        }
    }

    public String getPaycoId(String accessToken) {
        try {
            ResponseEntity<PaycoMemberResponseDto> response = paycoMemberInfoAdapter.getMemberInfo(
                clientId, accessToken);
            if (response.getStatusCode().is2xxSuccessful()) {
                PaycoMemberResponseDto responseDto = response.getBody();
                if (responseDto != null) {
                    return responseDto.data().member().idNo();
                }
            }
            throw new PaycoGetMemberInfoFailedException();
        }
        catch (FeignException e) {
            throw new PaycoGetMemberInfoFailedException();
        }
    }

    public String getMemberId(String paycoId){
        try{
            ResponseEntity<MemberLoginIdResponseDto> response = shopUserAdapter.getMemberIdByPaycoId(paycoId);
            if (response.getStatusCode().is2xxSuccessful()) {
                MemberLoginIdResponseDto responseDto = response.getBody();
                if (responseDto != null) {
                    return responseDto.loginId();
                }
            }
            throw new PaycoGetMemberInfoFailedException("get member id failed");
        }
        catch (FeignException e) {
            if (e.status() == 404){
                throw new MemberNotFoundException();
            }
            throw new PaycoGetMemberInfoFailedException("get member id failed");
        }
    }

    public String linkAccount(String paycoId, String accessToken){
        try{
            ResponseEntity<MessageDto> response = shopUserAdapter.linkAccount(paycoId, accessToken);
            if (response.getStatusCode().is2xxSuccessful()) {
                MessageDto responseDto = response.getBody();
                if (responseDto != null) {
                    return responseDto.message();
                }
            }
            throw new PaycoLinkAccountFailedException("payco link account failed");
        }
        catch (FeignException e) {
            if (e.status() == 409){
                throw new AlreadyLinkAccountException("already link account");
            }
            throw new PaycoLinkAccountFailedException("payco link account failed");
        }
    }
}
