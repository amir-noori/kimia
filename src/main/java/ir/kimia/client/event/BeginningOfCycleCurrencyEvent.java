package ir.kimia.client.event;

import ir.kimia.client.controller.model.CurrencyBalance;

import java.util.List;


/**
 * Event for sending currency stocks data between @{@link ir.kimia.client.controller.PartyManagementController}
 *  and @{@link ir.kimia.client.controller.CurrencyPopupController}
 *
 *  @author Amir
 */
public class BeginningOfCycleCurrencyEvent {

    List<CurrencyBalance> currencyStocks;

    public BeginningOfCycleCurrencyEvent(List<CurrencyBalance> currencyStocks) {
        this.currencyStocks = currencyStocks;
    }

    public List<CurrencyBalance> getCurrencyStocks() {
        return currencyStocks;
    }

    public void setCurrencyStocks(List<CurrencyBalance> currencyStocks) {
        this.currencyStocks = currencyStocks;
    }
}
