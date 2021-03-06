package models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Параметры скидки при покупке связанных товаров
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemGroupRules {

    // Коэффициент скидки (сидка = цена * discount)
    private double discount;

    // ID группы связанных товаров
    private List<String> groupId;

    // 'Номер магазина, -1 для акции сети'
    private Integer shopId;
}
