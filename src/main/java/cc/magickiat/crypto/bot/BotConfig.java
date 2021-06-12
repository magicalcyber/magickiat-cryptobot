package cc.magickiat.crypto.bot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotConfig {
    private final Properties prop = new Properties();

    public BotConfig(String configFile) throws IOException {
        try (InputStream is = new FileInputStream(configFile)) {
            prop.load(is);
        }
    }

    public String getStrategyName() {
        return prop.getProperty("trade.strategy");
    }

    public String getStrategyConfig() {
        return prop.getProperty("trade.strategy.config");
    }

    public String getTradeSymbol() {
        return prop.getProperty("trade.symbol");
    }

    public String getTimeframe() {
        return prop.getProperty("trade.timeframe");
    }

    public String getBuyStrategyName() {
        return prop.getProperty("trade.strategy.buy");
    }

    public String getSellStrategyName() {
        return prop.getProperty("trade.strategy.sell");
    }

    public String getBuyConfig(String configName) {
        return prop.getProperty("trade.strategy.buy." + configName);
    }

    public String getSellConfig(String configName) {
        return prop.getProperty("trade.strategy.sell." + configName);
    }
}
