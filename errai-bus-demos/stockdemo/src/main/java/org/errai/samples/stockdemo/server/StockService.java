package org.errai.samples.stockdemo.server;

import com.google.inject.Inject;
import org.errai.samples.stockdemo.client.Stock;
import org.jboss.errai.bus.client.api.*;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.framework.SubscriptionEvent;
import org.jboss.errai.bus.server.annotations.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


@Service
public class StockService implements MessageCallback {

    private Map<String, Stock> stocks = new HashMap<String, Stock>();
    private List<String> tickerList = new CopyOnWriteArrayList<String>();
    private volatile AsyncTask task;

    @Inject
    public StockService(final RequestDispatcher dispatcher, final MessageBus bus) {
        loadDefault();

        bus.addSubscribeListener(new SubscribeListener() {
            public void onSubscribe(SubscriptionEvent event) {
                if (event.getSubject().equals("StockClient")) {
                    if (task == null) {
                        task = MessageBuilder.createMessage()
                                .toSubject("StockClient")
                                .command("PriceChange")
                                .withProvided("Data", new ResourceProvider<String>() {
                                    public String get() {
                                        return simulateRandomChange();
                                    }
                                })
                                .noErrorHandling()
                                .sendRepeatingWith(dispatcher, TimeUnit.MILLISECONDS, 100);
                    }
                }
            }
        });
    }

    public void callback(Message message) {
        if ("Start".equals(message.getCommandType())) {

            for (Stock stock : stocks.values()) {
                MessageBuilder.createConversation(message)
                        .toSubject("StockClient")
                        .command("UpdateStockInfo")
                        .with("Stock", stock)
                        .noErrorHandling().reply();
            }

        } else if ("GetStockInfo".equals(message.getCommandType())) {
            Stock stock = stocks.get(message.get(String.class, "Ticker"));

            MessageBuilder.createConversation(message)
                    .toSubject("StockClient")
                    .command("UpdateStockInfo")
                    .with("Stock", stock)
                    .noErrorHandling().reply();
        }

    }

    public String simulateRandomChange() {
        String ticker = tickerList.get((int) (Math.random() * 1000) % tickerList.size());

        Stock stock = stocks.get(ticker);

        if (Math.random() > 0.5d) {
            double price = stock.getLastTrade();

            if (Math.random() > 0.9d) {
                price += Math.random() * 0.05;
            } else if (Math.random() < 0.1d) {
                price -= Math.random() * 0.05;
            }

            stock.setLastTrade(price);
        }

        return stock.getTicker() + ":" + stock.getLastTrade();
    }

    public void addEquity(String ticker, String company, double lastTrade) {
        stocks.put(ticker, new Stock(ticker, company, lastTrade));
        tickerList.add(ticker);
    }

    public void loadDefault() {
        addEquity("FUN", "FunCo", 10.28);
        addEquity("FOO", "Foobar Worldco", 8.3);
        addEquity("GWTC", "The GWT Company", 5.2);
        addEquity("FGC", "Fun Gaming Corporation", 19.3);
        addEquity("XXX", "Triple X", 40.2);
    }

}