package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BodyRequestReceipt {

      private String shopId;

      private Boolean loyaltyCard;

      private List<Position> positions;

}
