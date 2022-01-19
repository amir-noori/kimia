package ir.kimia.client.controller.model;

import java.util.List;

public class InvoicePrintData {

    private List<InvoiceRecordPrintData> invoiceRecordPrintDataList;
    private String totalAmount;
    private String partyCode;
    private String partyName;
    private String date;
    private String documentNumber;
    private String invoiceNumber;
    private String previousBalance;
    private String invoiceBalance;
    private String totalBalance;

    public List<InvoiceRecordPrintData> getInvoiceRecordPrintDataList() {
        return invoiceRecordPrintDataList;
    }

    public void setInvoiceRecordPrintDataList(List<InvoiceRecordPrintData> invoiceRecordPrintDataList) {
        this.invoiceRecordPrintDataList = invoiceRecordPrintDataList;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPartyCode() {
        return partyCode;
    }

    public void setPartyCode(String partyCode) {
        this.partyCode = partyCode;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(String previousBalance) {
        this.previousBalance = previousBalance;
    }

    public String getInvoiceBalance() {
        return invoiceBalance;
    }

    public void setInvoiceBalance(String invoiceBalance) {
        this.invoiceBalance = invoiceBalance;
    }

    public String getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(String totalBalance) {
        this.totalBalance = totalBalance;
    }

    public static class InvoiceRecordPrintData {

        private String carat;
        private String amount;
        private String count;
        private String description;
        private String weight;
        private String fee;

        public String getCarat() {
            return carat;
        }

        public void setCarat(String carat) {
            this.carat = carat;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getFee() {
            return fee;
        }

        public void setFee(String fee) {
            this.fee = fee;
        }
    }


}
