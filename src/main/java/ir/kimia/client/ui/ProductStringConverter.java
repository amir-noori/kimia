package ir.kimia.client.ui;

import ir.kimia.client.data.model.Product;
import javafx.util.StringConverter;

import java.util.List;

public class ProductStringConverter extends StringConverter<Product> {

    private List<Product> allProducts;

    public ProductStringConverter(List<Product> allParties) {
        this.allProducts = allParties;
    }

    @Override
    public String toString(Product object) {
        if (object == null || (object.getProductName() == null && object.getCode() == null)) return "";
        if (object.getProductName() == null && object.getCode() != null) {
            return String.valueOf(object.getCode());
        }
        return object.getProductName();
    }

    @Override
    public Product fromString(String string) {
        Product result = new Product();
        if (allProducts != null) {
            for (Product object : allProducts) {
                if (object.getProductName().equals(string)) {
                    result.setProductCategory(object.getProductCategory());
                    result.setCarat(object.getCarat());
                    result.setOffice(object.getOffice());
                    result.setProductName(object.getProductName());
                    result.setPayedWagePercentage(object.getPayedWagePercentage());
                    result.setPayedWageAmount(object.getPayedWageAmount());
                    result.setCode(object.getCode());
                    result.setId(object.getId());
                    result.setReceivedWagePercentage(object.getReceivedWagePercentage());
                    result.setReceivedWageAmount(object.getReceivedWageAmount());
                    return result;
                }
            }
        }
        result.setProductName(string);
        return result;
    }

}
