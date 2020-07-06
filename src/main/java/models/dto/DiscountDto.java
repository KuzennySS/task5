package models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountDto {

    // Итоговая скидка по акции
    private BigDecimal percent;

    // Количество единиц товара в подарок
    private Integer bonusQuantity;
}
