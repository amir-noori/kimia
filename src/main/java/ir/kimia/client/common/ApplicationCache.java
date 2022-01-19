package ir.kimia.client.common;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ir.kimia.client.data.model.Product;
import ir.kimia.client.service.api.ProductService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationCache {

    private static Cache<String, Object> cache = Caffeine.newBuilder().build();

    private static Map<String, List<Product>> productsCacheMap = new HashMap<>();

    public static final String PRODUCT_CATEGORY = "product";

    private ProductService productService;

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    public void remove(String cacheCategory, String key, Object object) {
        switch (cacheCategory) {
            case PRODUCT_CATEGORY -> {
                List<Product> productsCache = productsCacheMap.get(key);
                if (productsCache != null) {
                    int indexOfProduct = productsCache.indexOf(object);
                    if (indexOfProduct != -1) {
                        productsCache.remove(indexOfProduct);
                    }
                }
            }
        }
    }

    public void set(String cacheCategory, String key, Object object) {
        if (object instanceof List<?>) {
            setList(cacheCategory, key, (List<Object>) object);
            return;
        }

        switch (cacheCategory) {
            case PRODUCT_CATEGORY -> {
                List<Product> productsCache = productsCacheMap.get(key);
                if (productsCache == null) {
                    productsCache = new ArrayList<>();
                    productsCacheMap.put(key, productsCache);
                }
                int indexOfProduct = productsCache.indexOf(object);
                if (indexOfProduct == -1) {
                    productsCache.add((Product) object);
                } else {
                    productsCache.set(indexOfProduct, (Product) object);
                }
            }
        }
    }

    public void setList(String cacheCategory, String key, List<Object> objectList) {
        switch (cacheCategory) {
            case PRODUCT_CATEGORY -> {
                List<Product> productsCache = productsCacheMap.get(key);
                if (productsCache == null) {
                    productsCache = new ArrayList<>();
                    productsCacheMap.put(key, productsCache);
                }
                if (objectList != null) {
                    productsCache = (List<Product>) (List<?>) objectList;
                    productsCacheMap.put(key, productsCache);
                }
            }
        }
    }

    public List<?> get(String cacheCategory, String key) {
        switch (cacheCategory) {
            case PRODUCT_CATEGORY -> {
                return productsCacheMap.get(key);
            }
        }

        return null;
    }

}
