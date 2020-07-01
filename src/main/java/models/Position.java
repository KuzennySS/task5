package models;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {

    private String itemId;

    private String quantity;
}


