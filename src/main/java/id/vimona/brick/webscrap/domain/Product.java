package id.vimona.brick.webscrap.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private String name;
    private String description;
    private String imageLink;
    private String price;
    private String rating;
    private String merchant;
}
