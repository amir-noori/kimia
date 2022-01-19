package ir.kimia.client.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;


@DatabaseTable(tableName = "TBL_INVOICE_RECORD")
public class InvoiceRecord {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "product_id")
    private Product product;

    @DatabaseField
    private InvoiceRecordType invoiceRecordType;

    @DatabaseField(canBeNull = false)
    private Integer dealType;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, columnName = "invoice_id")
    private Invoice invoice;

    @DatabaseField
    private Double carat;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "payment_order_party_id")
    private Party paymentOrderParty;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "bank_id")
    private Bank bank;

    @DatabaseField
    private Double wagePercentage;

    @DatabaseField
    private Double fee;

    @DatabaseField
    private Double tax;

    @DatabaseField
    private Double quote;

    @DatabaseField
    private Integer count;

    @DatabaseField
    private Double weightByScale;

    @DatabaseField
    private Double benefit;

    @DatabaseField
    private Double amount;

    @DatabaseField
    private String description;

    @ForeignCollectionField(eager = true)
    private Collection<Stone> stones;

    // this is used when a melted stock is going to be sold
    private Stock meltedStock;

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Stock getMeltedStock() {
        return meltedStock;
    }

    public void setMeltedStock(Stock meltedStock) {
        this.meltedStock = meltedStock;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Product getProduct() {
        return product;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public Party getPaymentOrderParty() {
        return paymentOrderParty;
    }

    public void setPaymentOrderParty(Party paymentOrderParty) {
        this.paymentOrderParty = paymentOrderParty;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getDealType() {
        return dealType;
    }

    public void setDealType(Integer dealType) {
        this.dealType = dealType;
    }

    public Double getCarat() {
        return carat;
    }

    public void setCarat(Double carat) {
        this.carat = carat;
    }

    public Double getWagePercentage() {
        return wagePercentage;
    }

    public void setWagePercentage(Double wagePercentage) {
        this.wagePercentage = wagePercentage;
    }

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public Double getTax() {
        return tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public InvoiceRecordType getInvoiceRecordType() {
        return invoiceRecordType;
    }

    public void setInvoiceRecordType(InvoiceRecordType invoiceRecordType) {
        this.invoiceRecordType = invoiceRecordType;
    }

    public Collection<Stone> getStones() {
        return stones;
    }

    public void setStones(Collection<Stone> stones) {
        this.stones = stones;
    }

    public Double getQuote() {
        return quote;
    }

    public void setQuote(Double quote) {
        this.quote = quote;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getWeightByScale() {
        return weightByScale;
    }

    public void setWeightByScale(Double weightByScale) {
        this.weightByScale = weightByScale;
    }

    public Double getBenefit() {
        return benefit;
    }

    public void setBenefit(Double benefit) {
        this.benefit = benefit;
    }

    public InvoiceRecord() {
    }


    public enum InvoiceRecordType {
        UNDEFINED(0),
        CASH(1),
        MELTED(2),
        MSC(3),
        MANUFACTURED(4),
        CURRENCY(5),
        COIN(6),
        BANK(7),
        CHEQUE(8),
        GOLD_DEBIT_OR_DEPOSIT(9),
        CASH_DEBIT_OR_DEPOSIT(10),
        CURRENCY_DEBIT_OR_DEPOSIT(11),
        STONE(12),
        PAYMENT_ORDER_CASH(13),
        PAYMENT_ORDER_CURRENCY(14),
        PAYMENT_ORDER_GOLD(15),
        DISCOUNT(16),
        MELTED_CURRENCY_TRANSACTIONS(17),
        MANUFACTURED_CURRENCY_TRANSACTIONS(18),
        CASH_TO_GOLD_CONVERSION(19),
        GOLD_TO_CASH_CONVERSION(20),
        PRODUCT(100);


        private int value;

        InvoiceRecordType(int i) {
            this.value = i;
        }

        public int value() {
            return this.value;
        }

        public static InvoiceRecordType getByValue(int value) {
            for (InvoiceRecordType invoiceRecordType : InvoiceRecordType.values()) {
                if (value == invoiceRecordType.value) {
                    return invoiceRecordType;
                }
            }
            return null;
        }

    }

    public enum DealType {
        PURCHASE(0),
        SELL(1),
        RECEIVE(2),
        PAYMENT(3),
        PURCHASE_BY_CASH(4),
        SELL_BY_CASH(5),
        RECEIVE_RETURNED(6),
        PAYMENT_RETURNED(7),
        CASH_AND_CURRENCY(8);


        private int value;

        DealType(int i) {
            this.value = i;
        }

        public int value() {
            return this.value;
        }

        public static DealType getByValue(Integer value) {
            if (value == null) return null;
            for (DealType dealType : DealType.values()) {
                if (value == dealType.value) {
                    return dealType;
                }
            }
            return null;
        }

    }


}
