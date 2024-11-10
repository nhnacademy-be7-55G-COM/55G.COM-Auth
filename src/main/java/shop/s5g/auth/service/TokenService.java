package shop.s5g.auth.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.s5g.auth.adapter.ShopUserAdapter;
import shop.s5g.auth.dto.TokenResponseDto;
import shop.s5g.auth.dto.UserDetailResponseDto;
import shop.s5g.auth.exception.InvalidResponseException;
import shop.s5g.auth.jwt.JwtUtil;
import shop.s5g.auth.repository.RefreshTokenRepository;
import shop.s5g.auth.repository.UUIDRepository;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UUIDRepository uuidRepository;
    private final ShopUserAdapter shopUserAdapter;

    public TokenResponseDto issueToken(String username, String role) {
        String uuid = UUID.randomUUID().toString();

        while (uuidRepository.existsUUID(uuid)) {
            uuid = UUID.randomUUID().toString();
        }

        uuidRepository.saveLoginIdAndRole(uuid, username, role);

        String accessToken = jwtUtil.createAccessToken(uuid);
        String refreshToken = jwtUtil.createRefreshToken(uuid);

        refreshTokenRepository.saveRefreshToken(uuid, refreshToken);

        if (role.equals("ROLE_MEMBER")){
            shopUserAdapter.updateLatestLoginAt(username);
        }

        return new TokenResponseDto(accessToken, refreshToken);
    }

//    public TokenResponseDto reissueToken(String refreshToken, String path) {
//        String loginId = jwtUtil.getUsername(refreshToken);
//
//        if (!refreshTokenRepository.isExistRefreshToken(loginId)){
//             throw new InvalidResponseException("Invalid refresh token");
//        }
//
//        String validRefreshToken = refreshTokenRepository.getRefreshToken(loginId);
//
//        if (!validRefreshToken.equals(refreshToken)) {
//            throw new InvalidResponseException("Invalid refresh token");
//        }
//
//        refreshTokenRepository.deleteRefreshToken(loginId);
//
//        return issueToken(loginId,);
//    }

    public boolean deleteToken(String uuid) {
        if (!refreshTokenRepository.isExistRefreshToken(uuid)){
            return false;
        }
        if (!uuidRepository.existsUUID(uuid)) {
            return false;
        }

        uuidRepository.deleteUUID(uuid);
        refreshTokenRepository.deleteRefreshToken(uuid);
        return true;
    }

    public UserDetailResponseDto getUserByUUID(String uuid){
        if (!uuidRepository.existsUUID(uuid)) {
            throw new InvalidResponseException("Invalid UUID");
        }
        String value = uuidRepository.getLoginIdAndRole(uuid);
        String[] parts = value.split(":");
        return new UserDetailResponseDto(parts[0], parts[1]);
    }
}
