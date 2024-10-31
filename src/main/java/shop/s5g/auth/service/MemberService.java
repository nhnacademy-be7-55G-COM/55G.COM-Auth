package shop.s5g.auth.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import shop.s5g.auth.adapter.MemberAdapter;
import shop.s5g.auth.dto.LoginResponseDto;
import shop.s5g.auth.exception.InvalidResponseException;
import shop.s5g.auth.exception.MemberNotFoundException;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberAdapter memberAdapter;

    public UserDetails getMember(String loginId){
        ResponseEntity<LoginResponseDto> response = memberAdapter.getUserInfo(loginId);

        try{
            if (response.getStatusCode().is2xxSuccessful()){
                LoginResponseDto responseDto = response.getBody();
                if (responseDto == null){
                    throw new InvalidResponseException("body is empty");
                }
                return new User(responseDto.loginId(), responseDto.password(), List.of(new SimpleGrantedAuthority("ROLE_MEMBER")));
            }
            throw new MemberNotFoundException("member cannot be found");
        }
        catch (HttpClientErrorException | HttpServerErrorException e){
            throw new MemberNotFoundException("member cannot be found");
        }
    }
}
