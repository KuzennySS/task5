package models;

import lombok.*;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    private String id;

    private String name;

    private String groupId;

    private BigDecimal price;
}
