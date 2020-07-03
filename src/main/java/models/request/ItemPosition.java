package models.request;

import lombok.*;

/**
 * Позиция товара в корзине
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPosition {

    // ID товара
    private String itemId;

    // Количество товара в позиции
    private String quantity;
}


