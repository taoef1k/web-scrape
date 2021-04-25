package id.vimona.brick.webscrap.domain.response;

import java.util.List;

import id.vimona.brick.webscrap.domain.Product;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private String status;
    private int total;
    private int page;
    private List<Product> products;
}
