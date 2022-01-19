package ir.kimia.client.controller.model;

import ir.kimia.client.data.model.AccountBalance;
import ir.kimia.client.data.model.Party;

import java.util.List;

public class AccountBalanceModel {

    private Party party;
    private AccountBalance goldBalance;
    private AccountBalance cashBalance;
    private List<AccountBalance> coinBalanceList;
    private List<AccountBalance> currencyBalanceList;

    public AccountBalanceModel() {
    }

    public AccountBalanceModel(Party party, AccountBalance goldBalance, AccountBalance cashBalance) {
        this.party = party;
        this.goldBalance = goldBalance;
        this.cashBalance = cashBalance;
    }

    public AccountBalanceModel(Party party, AccountBalance goldBalance, AccountBalance cashBalance, List<AccountBalance> coinBalanceList, List<AccountBalance> currencyBalanceList) {
        this.party = party;
        this.goldBalance = goldBalance;
        this.cashBalance = cashBalance;
        this.coinBalanceList = coinBalanceList;
        this.currencyBalanceList = currencyBalanceList;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public AccountBalance getGoldBalance() {
        return goldBalance;
    }

    public void setGoldBalance(AccountBalance goldBalance) {
        this.goldBalance = goldBalance;
    }

    public AccountBalance getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(AccountBalance cashBalance) {
        this.cashBalance = cashBalance;
    }

    public List<AccountBalance> getCoinBalanceList() {
        return coinBalanceList;
    }

    public void setCoinBalanceList(List<AccountBalance> coinBalanceList) {
        this.coinBalanceList = coinBalanceList;
    }

    public List<AccountBalance> getCurrencyBalanceList() {
        return currencyBalanceList;
    }

    public void setCurrencyBalanceList(List<AccountBalance> currencyBalanceList) {
        this.currencyBalanceList = currencyBalanceList;
    }
}
