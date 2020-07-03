package models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Параметры скидки при предъявлении пенсионного удостоверения или социальной карты
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyCardRule {

    // Коэффициент скидки (сидка = цена * discount)
    private double discount;

    // 'Номер магазина, -1 для акции сети'
    private Integer shopId;
}
