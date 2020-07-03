package models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Данные о магазине и товарах в корзине
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCart {

      // Номер магазина
      private Integer shopId;

      // Признак предоставленния карты лояльности
      private Boolean loyaltyCard;

      // Позиции с товарами в корзине
      private List<ItemPosition> positions;

}
