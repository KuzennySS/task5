package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import models.dto.PositionDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BodyAnswerReceipt {

      private Double total;

      private Double discount;

      private List<PositionDto> positionDtos;
}
