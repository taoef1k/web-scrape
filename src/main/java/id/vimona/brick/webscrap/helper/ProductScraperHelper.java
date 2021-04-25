package id.vimona.brick.webscrap.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import id.vimona.brick.webscrap.domain.Product;
import id.vimona.brick.webscrap.domain.enums.ProductAttribute;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductScraperHelper {

    private String pageUrl;
    private Integer pageParseTimeoutMillis;
    private List<String> searchTags;
    private Map<ProductAttribute, String> productAttributes;

    public ProductScraperHelper(String pageUrl, Integer pageParseTimeoutMillis, List<String> searchTags,
            Map<ProductAttribute, String> productAttributes) {
        this.pageUrl = pageUrl;
        this.pageParseTimeoutMillis = pageParseTimeoutMillis;
        this.searchTags = searchTags;
        this.productAttributes = productAttributes;
    }

    public CompletableFuture<List<Product>> fetchAllProductsFromPage() {
        List<Product> detailList = new ArrayList<>();

        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.getAllProductLinksFromPage();
            } catch (IOException e) {
                log.error("Error in getting links.", e);
            }
            return null;
        }).thenApply(links -> {
            links.forEach(link -> {
                CompletableFuture<Product> detailsFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return getProductDetails(link);
                    } catch (IOException e) {
                        log.error("Error in getting link details.", e);
                    }
                    return null;
                });
                try {
                    detailList.add(detailsFuture.get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error in extracting results after task completion.", e);
                }
            });
            return detailList;
        }).toCompletableFuture();
    }

    private Set<String> getAllProductLinksFromPage() throws IOException {
        Document doc = Jsoup.connect(pageUrl).userAgent("Mozilla/5.0").timeout(pageParseTimeoutMillis).get();
        return searchLinkTags(doc, searchTags);
    }

    private Set<String> searchLinkTags(Document doc, List<String> searchTags) {
        Set<String> links = new HashSet<>();
        List<Elements> elements = searchTags.stream().map(tag -> doc.select(tag)).collect(Collectors.toList());
        elements.forEach(element -> element.forEach(e -> links.add(e.select("a[href]").attr("href"))));
        return links;
    }

    private Product getProductDetails(String url) throws IOException {
        try {
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(pageParseTimeoutMillis).get();
            return Product.builder().name(this.getProductNameAttribute(doc))
                    .description(this.getProductDescriptionAttribute(doc))
                    .imageLink(this.getProductImageLinkAttribute(doc)).price(this.getProductPriceAttribute(doc))
                    .rating(this.getProductRatingAttribute(doc)).merchant(this.getProductMerchantAttribute(doc))
                    .build();
        } catch (Exception e) {
            log.error("ERROR:", e);
        }
        return new Product();
    }

    private String getProductNameAttribute(Document doc) {
        return doc.select(productAttributes.get(ProductAttribute.NAME)).text();
    }

    private String getProductDescriptionAttribute(Document doc) {
        return doc.select(productAttributes.get(ProductAttribute.DESCRIPTION)).text();
    }

    private String getProductImageLinkAttribute(Document doc) {
        Elements elements = doc.select(productAttributes.get(ProductAttribute.IMAGELINK));
        return elements.select("img[src]").attr("src");
    }

    private String getProductPriceAttribute(Document doc) {
        return doc.select(productAttributes.get(ProductAttribute.PRICE)).text();
    }

    private String getProductRatingAttribute(Document doc) {
        return doc.select(productAttributes.get(ProductAttribute.RATING)).text();
    }

    private String getProductMerchantAttribute(Document doc) {
        return doc.select(productAttributes.get(ProductAttribute.MERCHANT)).attr("href");
    }

}
