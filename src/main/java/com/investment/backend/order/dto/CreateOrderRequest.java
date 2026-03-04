package com.investment.backend.order.dto;

import com.investment.backend.order.enums.OrderType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "계좌 ID는 필수입니다.")
    private UUID accountId;

    @NotBlank(message = "종목 코드는 필수입니다.")
    private String stockCode;

    @NotBlank(message = "종목명은 필수입니다.")
    private String stockName;

    @NotNull(message = "주문 가격은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "주문 가격은 0보다 커야 합니다.")
    private BigDecimal orderPrice;

    @NotNull(message = "주문 수량은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "주문 수량은 0보다 커야 합니다.")
    private BigDecimal quantity;

    @NotNull(message = "주문 유형은 필수입니다.")
    private OrderType orderType;
}
