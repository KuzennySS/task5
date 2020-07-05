package models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Матрица промо-механик
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoMatrix {

    // Список правил при покупке определенного количества единиц конкретного товара
    private List<ItemCountRules> itemCountRules;

    // Список правил при покупке связанных товаров
    private List<ItemGroupRules> itemGroupRules;

    // Список правил скидки при предъявлении пенсионного удостоверения или социальной карты
    private List<LoyaltyCardRule> loyaltyCardRules;

}
