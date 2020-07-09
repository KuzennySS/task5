package models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionNaddK {

    // Название бонусной позиции
    private String idPosition;

    // тригер для бонуса
    private Integer trigerQuantity;

    // Количество по бонусной позиции
    private Integer bonusQuantity;

}
