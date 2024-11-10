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
import shop.s5g.auth.adapter.ShopUserAdapter;
import shop.s5g.auth.dto.LoginResponseDto;
import shop.s5g.auth.exception.AdminNotFoundException;
import shop.s5g.auth.exception.InvalidResponseException;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ShopUserAdapter shopUserAdapter;

    public UserDetails getAdmin(String loginId){
        ResponseEntity<LoginResponseDto> response = shopUserAdapter.getUserInfo(loginId);

        try{
            if (response.getStatusCode().is2xxSuccessful()){
                LoginResponseDto responseDto = response.getBody();
                if (responseDto == null){
                    throw new InvalidResponseException("body is empty");
                }
                return new User(responseDto.loginId(), responseDto.password(), List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            }
            throw new AdminNotFoundException("admin cannot be found");
        }
        catch (HttpClientErrorException | HttpServerErrorException e){
            throw new AdminNotFoundException("admin cannot be found");
        }
    }
}
