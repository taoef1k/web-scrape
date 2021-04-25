package id.vimona.brick.webscrap.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import id.vimona.brick.webscrap.domain.response.ProductResponse;
import id.vimona.brick.webscrap.service.ProductScraperService;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/handphone")
public class ProductScraperEndpoint {

    @Autowired
    private ProductScraperService productScraperService;

    @ApiOperation(value = "List of Handphone", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping
    public ResponseEntity<ProductResponse> listProducts() {
        return new ResponseEntity<>(productScraperService.getProducts(), HttpStatus.OK);
    }

    @ApiOperation(value = "Export To CSV", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(value = "/exportCSV", produces = "text/csv")
    public ResponseEntity<Resource> exportToCsv() {
        try {
            InputStreamResource csvFile = productScraperService.exportToCsv();

            HttpHeaders headers = new HttpHeaders();
            String filename = UUID.randomUUID().toString();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");

            return new ResponseEntity<>(csvFile, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, null, e);
        }
    }
}
