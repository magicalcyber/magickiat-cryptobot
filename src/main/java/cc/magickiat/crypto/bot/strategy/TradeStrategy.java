package cc.magickiat.crypto.bot.strategy;

import cc.magickiat.crypto.bot.constant.TradeAction;
import cc.magickiat.crypto.bot.dto.CandlestickDto;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

public interface TradeStrategy {
    TradeAction onCandlestickClosed(CandlestickDto candlestickDto);

    void initConfig(String configPath) throws IOException;

    void initCandlestick(List<CandlestickDto> candlesticks);

    default double[] getClosedPrice(List<CandlestickDto> candlesticks) {
        double[] result = new double[candlesticks.size()];
        for (int i = 0; i < candlesticks.size(); i++) {
            result[i] = candlesticks.get(i)
                    .getClose().setScale(4, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        return result;
    }

    default double[] createOutputArray(int outputSize) {
        double[] output = new double[outputSize];

        Arrays.fill(output, -1);

        return output;
    }
}
