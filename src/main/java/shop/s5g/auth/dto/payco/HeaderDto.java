package shop.s5g.auth.dto.payco;

public record HeaderDto(
    boolean isSuccessful,
    int resultCode,
    String resultMessage
) {}
