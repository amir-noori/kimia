package ir.kimia.client.event;

import ir.kimia.client.controller.model.CoinBalance;

import java.util.List;

/**
 * Event for sending coin stocks data between @{@link ir.kimia.client.controller.PartyManagementController}
 *  and @{@link ir.kimia.client.controller.CoinPopupController}
 *
 *  @author Amir
 */
public class BeginningOfCycleCoinEvent {

    List<CoinBalance> coinStocks;

    public BeginningOfCycleCoinEvent(List<CoinBalance> coinStocks) {
        this.coinStocks = coinStocks;
    }

    public List<CoinBalance> getCoinStocks() {
        return coinStocks;
    }

    public void setCoinStocks(List<CoinBalance> coinStocks) {
        this.coinStocks = coinStocks;
    }
}
