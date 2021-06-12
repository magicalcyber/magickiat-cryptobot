package cc.magickiat.crypto.bot.strategy;

import cc.magickiat.crypto.bot.BotConfig;
import cc.magickiat.crypto.bot.dto.CandlestickDto;

public interface BuyStrategy {
    void initConfig(BotConfig config);

    String getQuantity(CandlestickDto candlestickDto);
}
