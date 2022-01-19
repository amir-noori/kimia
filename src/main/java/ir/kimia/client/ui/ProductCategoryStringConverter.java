package ir.kimia.client.ui;

import ir.kimia.client.data.model.ProductCategory;
import javafx.util.StringConverter;

import java.util.List;

public class ProductCategoryStringConverter extends StringConverter<ProductCategory> {

    private List<ProductCategory> allProductCategories;

    public ProductCategoryStringConverter(List<ProductCategory> allProductCategories) {
        this.allProductCategories = allProductCategories;
    }

    @Override
    public String toString(ProductCategory object) {
        if (object == null || (object.getTitle() == null && object.getCode() == null)) return "";
        if (object.getTitle() == null && object.getCode() != null) {
            return String.valueOf(object.getCode());
        }
        return object.getTitle();
    }

    @Override
    public ProductCategory fromString(String string) {
        ProductCategory result = new ProductCategory();
        if (allProductCategories != null) {
            for (ProductCategory object : allProductCategories) {
                if (object.getTitle().equals(string)) {
                    result.setCountable(object.getCountable());
                    result.setDescription(object.getDescription());
                    result.setCode(object.getCode());
                    result.setId(object.getId());
                    result.setTitle(object.getTitle());
                    result.setModifiable(object.getModifiable());
                    result.setOffice(object.getOffice());
                    return result;
                }
            }
        }
        result.setTitle(string);
        return result;
    }

}
