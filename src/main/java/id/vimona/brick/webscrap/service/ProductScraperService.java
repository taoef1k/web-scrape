package id.vimona.brick.webscrap.service;

import java.io.IOException;

import org.springframework.core.io.InputStreamResource;

import id.vimona.brick.webscrap.domain.response.ProductResponse;

public interface ProductScraperService {
    void loadContents(int page) throws IOException;

    ProductResponse getProducts();

    InputStreamResource exportToCsv() throws IOException;
}
