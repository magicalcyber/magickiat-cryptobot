package cc.magickiat.crypto.bot.strategy;

import cc.magickiat.crypto.bot.BotConfig;
import cc.magickiat.crypto.bot.strategy.impl.EmaFamilyTradeStrategy;
import cc.magickiat.crypto.bot.strategy.impl.FixedAssetBuyStrategy;
import cc.magickiat.crypto.bot.strategy.impl.FixedAssetSellStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class TradeStrategyFactory {
    private static final Logger logger = LogManager.getLogger(TradeStrategyFactory.class);

    private TradeStrategyFactory() {
    }

    public static TradeStrategy createStrategy(String strategyName, String strategyConfigFile) throws IOException {
        TradeStrategy tradeStrategy = null;

        if ("ema-family".equalsIgnoreCase(strategyName)) {
            tradeStrategy = new EmaFamilyTradeStrategy();
        } else {
            try {
                Class<?> aClass = Class.forName(strategyName);
                tradeStrategy = (TradeStrategy) aClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        if (tradeStrategy != null) {
            tradeStrategy.initConfig(strategyConfigFile);
        }

        return tradeStrategy;
    }

    public static BuyStrategy createBuyStrategy(BotConfig config) {
        BuyStrategy buyStrategy = null;
        if ("fixed-asset".equalsIgnoreCase(config.getBuyStrategyName())) {
            buyStrategy = new FixedAssetBuyStrategy();
        } else {
            try {
                Class<?> aClass = Class.forName(config.getBuyStrategyName());
                buyStrategy = (BuyStrategy) aClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        if (buyStrategy != null) {
            buyStrategy.initConfig(config);
        }
        return buyStrategy;
    }

    public static SellStrategy createSellStrategy(BotConfig config) {
        SellStrategy sellStrategy = null;
        if ("fixed-asset".equalsIgnoreCase(config.getSellStrategyName())) {
            sellStrategy = new FixedAssetSellStrategy();
        } else {
            try {
                Class<?> aClass = Class.forName(config.getSellStrategyName());
                sellStrategy = (SellStrategy) aClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        if (sellStrategy != null) {
            sellStrategy.initConfig(config);
        }
        return sellStrategy;
    }
}
