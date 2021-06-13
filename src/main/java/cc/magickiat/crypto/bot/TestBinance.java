package cc.magickiat.crypto.bot;

import cc.magickiat.crypto.bot.constant.TradeAction;
import cc.magickiat.crypto.bot.dto.CandlestickDto;
import cc.magickiat.crypto.bot.strategy.BuyStrategy;
import cc.magickiat.crypto.bot.strategy.SellStrategy;
import cc.magickiat.crypto.bot.strategy.TradeStrategy;
import cc.magickiat.crypto.bot.strategy.TradeStrategyFactory;
import cc.magickiat.crypto.bot.utils.CandlestickConverter;
import cc.magickiat.crypto.bot.utils.TradeLogger;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.binance.api.client.domain.account.NewOrder.marketBuy;
import static com.binance.api.client.domain.account.NewOrder.marketSell;

public class TestBinance {
    private static final Logger logger = LogManager.getLogger(Trader.class);

    public static void main(String[] args) throws IOException {
        logger.info("================================");
        logger.info("\tMagicKiat CryptoBot Started!!!");
        logger.info("================================");

        // Load config
        BotConfig config = new BotConfig("config/bot.properties");
        String symbol = config.getTradeSymbol();
        String timeframeConfig = config.getTimeframe();
        CandlestickInterval candlestickInterval = Arrays.stream(CandlestickInterval.values())
                .sequential()
                .filter(e -> e.getIntervalId().equalsIgnoreCase(timeframeConfig))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid timeframe config: " + timeframeConfig));


        // Prepare Binance client
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(System.getenv("BINANCE_API_KEY"), System.getenv("BINANCE_API_SECRET"));
        BinanceApiRestClient restClient = factory.newRestClient();

        // Init trade strategy
        TradeStrategy tradeStrategy = TradeStrategyFactory.createStrategy(config.getStrategyName(), config.getStrategyConfig());
        if (tradeStrategy == null) {
            throw new IllegalArgumentException("Not found strategy name: " + config.getStrategyName());
        }

        BuyStrategy buyStrategy = TradeStrategyFactory.createBuyStrategy(config);
        if (buyStrategy == null) {
            throw new IllegalArgumentException("Not found buy strategy name: " + config.getBuyStrategyName());
        }

        SellStrategy sellStrategy = TradeStrategyFactory.createSellStrategy(config);
        if (sellStrategy == null) {
            throw new IllegalArgumentException("Not found sell strategy name: " + config.getSellStrategyName());
        }

        // Init candlesticks for strategy
        List<Candlestick> candlestickBars = restClient.getCandlestickBars(symbol.toUpperCase(), CandlestickInterval.ONE_MINUTE);
        List<CandlestickDto> oldCandlesticks = candlestickBars.stream().map(CandlestickConverter::mapCandlestick).collect(Collectors.toList());
        oldCandlesticks.remove(oldCandlesticks.size() - 1); // remove last candlestick because it not closed
        tradeStrategy.initCandlestick(oldCandlesticks);

        // Begin retrieve candlestick stream
        logger.info(">>>>> Begin retrieve candlestick <<<<<");
        TradeLogger tradeLogger = new TradeLogger();

        final String symbolUpperCase = symbol.toUpperCase();
        BinanceApiWebSocketClient webSocketKLine = factory.newWebSocketClient();
        final Closeable closeable = webSocketKLine.onCandlestickEvent(symbol.toLowerCase(), candlestickInterval, e -> {
            if (e.getBarFinal()) {
                CandlestickDto candlestickDto = CandlestickConverter.mapCandlestickEvent(e);
                logger.info("Closed candlestick data: {}", candlestickDto.toString());

                TradeAction tradeAction = tradeStrategy.onCandlestickClosed(candlestickDto);

                tradeLogger.writeLog(tradeAction, symbolUpperCase, candlestickDto);
                String buyQuantity = buyStrategy.getQuantity(candlestickDto);
                logger.info("BUY signal@{}", candlestickDto.getCloseTime());
                logger.info("BUY quantity: {}", buyQuantity);
                restClient.newOrderTest(marketBuy(symbolUpperCase, buyQuantity));
                logger.info("Place buy order complete");
            }
        });

        // graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                logger.info("Releasing resources");
                closeable.close();
                tradeLogger.close();
                logger.info("Released resources");
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }));

    }
}
