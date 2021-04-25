package id.vimona.brick.webscrap.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import id.vimona.brick.webscrap.domain.Product;
import id.vimona.brick.webscrap.domain.enums.ProductAttribute;
import id.vimona.brick.webscrap.domain.response.ProductResponse;
import id.vimona.brick.webscrap.helper.ProductScraperHelper;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductScraperServiceImpl implements ProductScraperService {

    private static final EnumMap<ProductAttribute, String> productAttributes = new EnumMap<>(ProductAttribute.class);

    @Value("${tokopedia.url}")
    private String tokopediaUrl;

    @Value("${tokopedia.parse.timeout.ms}")
    private Integer parseTimeoutMillis;

    @Value("${tokopedia.handphone.name}")
    private String handphoneName;

    @Value("${tokopedia.handphone.description}")
    private String handphoneDescription;

    @Value("${tokopedia.handphone.imageLink}")
    private String handphoneImageLink;

    @Value("${tokopedia.handphone.price}")
    private String handphonePrice;

    @Value("${tokopedia.handphone.rating}")
    private String handphoneRating;

    @Value("${tokopedia.handphone.merchant}")
    private String handphoneMerchant;

    @Value("#{'${tokopedia.handphone.searchtags}'.split(',')}")
    private List<String> handphoneSearchTags;

    private List<Product> products = new ArrayList<>();
    private String status;
    private int loadPage = 1;

    @PostConstruct
    private void init() {
        productAttributes.put(ProductAttribute.NAME, handphoneName);
        productAttributes.put(ProductAttribute.DESCRIPTION, handphoneDescription);
        productAttributes.put(ProductAttribute.IMAGELINK, handphoneImageLink);
        productAttributes.put(ProductAttribute.PRICE, handphonePrice);
        productAttributes.put(ProductAttribute.RATING, handphoneRating);
        productAttributes.put(ProductAttribute.MERCHANT, handphoneMerchant);
        status = "Processing scraping page...";
        products.clear();

        try {
            this.loadContents(loadPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadContents(int page) throws IOException {
        log.info("loadContents()...start");

        ProductScraperHelper scraperHelper = new ProductScraperHelper(tokopediaUrl + "?page=" + page,
                parseTimeoutMillis, handphoneSearchTags, productAttributes);

        scraperHelper.fetchAllProductsFromPage().thenAccept(
                list -> list.stream().filter(product -> StringUtils.hasText(product.getName())).forEach(product -> {
                    log.info("Product: {}", product);
                    products.add(product);
                })).whenComplete((v, th) -> {
                    loadPage++;
                    if (products.size() < 100) {
                        try {
                            log.info("Re-run load product");
                            this.loadContents(loadPage);
                        } catch (IOException e) {
                            log.error("ERROR:", e);
                        }
                    } else {
                        status = "Done";
                    }
                });
    }

    @Override
    public ProductResponse getProducts() {
        return ProductResponse.builder().status(status).page(loadPage).total(products.size()).products(products)
                .build();
    }

    @Override
    public InputStreamResource exportToCsv() throws IOException {
        String[] csvHeader = { "name", "description", "imageLink", "price", "rating", "merchant" };

        List<List<String>> csvBody = Optional.ofNullable(products).orElseGet(Collections::emptyList).stream()
                .map(product -> Arrays.asList(product.getName(), product.getDescription(), product.getImageLink(),
                        product.getPrice(), product.getRating(), product.getMerchant()))
                .collect(Collectors.toList());

        ByteArrayInputStream byteArrayOutputStream;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT.withHeader(csvHeader));

        for (List<String> record : csvBody) {
            csvPrinter.printRecord(record);
        }

        csvPrinter.flush();

        byteArrayOutputStream = new ByteArrayInputStream(out.toByteArray());

        csvPrinter.close();

        return new InputStreamResource(byteArrayOutputStream);
    }

}
