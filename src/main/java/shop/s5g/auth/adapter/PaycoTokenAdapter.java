package shop.s5g.auth.adapter;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import shop.s5g.auth.dto.payco.PaycoResponseDto;

@FeignClient(name = "payco", url = "${oauth.payco.token-url}")
public interface PaycoTokenAdapter {

    @PostMapping
    ResponseEntity<PaycoResponseDto> getToken(@RequestParam Map<String, String> params);
}
