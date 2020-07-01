package models;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    private String id;

    private String name;

    private String groupId;

    private Double price;
}
