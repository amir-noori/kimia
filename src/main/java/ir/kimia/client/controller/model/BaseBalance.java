package ir.kimia.client.controller.model;

import ir.kimia.client.data.model.Product;

public class BaseBalance {
    private Product product;
    private Integer id;
    private Integer debitCount;
    private Integer creditCount;
    private Integer sellFee;
    private Integer purchaseFee;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDebitCount() {
        return debitCount;
    }

    public void setDebitCount(Integer debitCount) {
        this.debitCount = debitCount;
    }

    public Integer getCreditCount() {
        return creditCount;
    }

    public void setCreditCount(Integer creditCount) {
        this.creditCount = creditCount;
    }

    public Integer getSellFee() {
        return sellFee;
    }

    public void setSellFee(Integer sellFee) {
        this.sellFee = sellFee;
    }

    public Integer getPurchaseFee() {
        return purchaseFee;
    }

    public void setPurchaseFee(Integer purchaseFee) {
        this.purchaseFee = purchaseFee;
    }
}
