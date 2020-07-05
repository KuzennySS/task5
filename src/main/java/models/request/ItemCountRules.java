package models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Параметры скидки формата "N+k" (N+k единиц товара по цене N единиц)
 * Скидки для сети
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCountRules {

    // Количество единиц товара в подарок
    private Integer bonusQuantity;

    // ID товара
    private String itemId;

    //Номер магазина, -1 для акции сети
    private Integer shopId;

    // Количество единиц для применения скидки
    private Integer triggerQuantity;
}
