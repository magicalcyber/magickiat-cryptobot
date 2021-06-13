package cc.magickiat.crypto.bot.strategy.impl;

import cc.magickiat.crypto.bot.BotConfig;
import cc.magickiat.crypto.bot.dto.CandlestickDto;
import cc.magickiat.crypto.bot.strategy.BuyStrategy;
import org.apache.commons.lang3.StringUtils;

public class FixedAssetBuyStrategy implements BuyStrategy {

    private String amount;

    @Override
    public void initConfig(BotConfig config) {
        String strAmount = config.getSellConfig("fixed-asset.amount");
        if (StringUtils.isEmpty(strAmount)) {
            throw new IllegalArgumentException("Please config amount");
        }

        try {
            Double.parseDouble(strAmount);
            amount = strAmount;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid amount: " + strAmount);
        }
    }

    @Override
    public String getQuantity(CandlestickDto candlestickDto) {
        return amount;
    }
}
