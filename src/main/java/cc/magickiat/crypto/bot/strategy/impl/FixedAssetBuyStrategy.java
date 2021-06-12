package cc.magickiat.crypto.bot.strategy.impl;

import cc.magickiat.crypto.bot.BotConfig;
import cc.magickiat.crypto.bot.dto.CandlestickDto;
import cc.magickiat.crypto.bot.strategy.BuyStrategy;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FixedAssetBuyStrategy implements BuyStrategy {

    private Double amount;

    @Override
    public void initConfig(BotConfig config) {
        String strAmount = config.getBuyConfig("fixed-asset.amount");
        if (StringUtils.isEmpty(strAmount)) {
            throw new IllegalArgumentException("Please config amount");
        }

        try {
            amount = Double.parseDouble(strAmount);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid amount: " + strAmount);
        }
    }

    @Override
    public String getQuantity(CandlestickDto candlestickDto) {
        BigDecimal quantity = candlestickDto
                .getClose().divide(new BigDecimal(amount), RoundingMode.HALF_UP);

        return quantity.toPlainString();
    }
}
