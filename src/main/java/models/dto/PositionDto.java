package models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionDto {

      private String id;

      private String name;

      private Double price;

      private Double regularPrice;

}
