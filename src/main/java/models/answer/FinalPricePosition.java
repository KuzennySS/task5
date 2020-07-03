package models.answer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Результат расчета цены для одной позиции
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalPricePosition {

      // ID товара
      private String id;

      // Наименование товара
      private String name;

      // Цена после применения скидки
      private BigDecimal price;

      // Цена до применения скидки
      private BigDecimal regularPrice;

}
