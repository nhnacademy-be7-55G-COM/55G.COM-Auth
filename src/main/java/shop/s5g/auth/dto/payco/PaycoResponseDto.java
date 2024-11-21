package shop.s5g.auth.dto.payco;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaycoResponseDto(
    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("access_token_secret")
    String accessTokenSecret,

    @JsonProperty("refresh_token")
    String refreshToken,

    @JsonProperty("token_type")
    String tokenType,

    @JsonProperty("expires_in")
    String expireIn,

    String state
) {

}
