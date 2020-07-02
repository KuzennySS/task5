package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.dto.Positions;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BodyAnswerReceipt {

      private BigDecimal total;

      private BigDecimal discount;

      private List<Positions> positions;
}
