package models.answer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Результат расчета корзины
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalPriceReceipt {

      // Полная сумма чека с применением скидок
      private BigDecimal total;

      // Полная сумма скидки
      private BigDecimal discount;

      // Результат расчета цен по позициям
      private List<FinalPricePosition> positions;
}
