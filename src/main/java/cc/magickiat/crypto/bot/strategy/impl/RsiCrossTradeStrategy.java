package cc.magickiat.crypto.bot.strategy.impl;

import cc.magickiat.crypto.bot.constant.TradeAction;
import cc.magickiat.crypto.bot.dto.CandlestickDto;
import cc.magickiat.crypto.bot.strategy.TradeStrategy;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RsiCrossTradeStrategy implements TradeStrategy {
    private static final Logger logger = LogManager.getLogger(RsiCrossTradeStrategy.class);
    private final Core taCore = new Core();

    private List<CandlestickDto> candlesticks = new ArrayList<>();

    private final int smaFastPeriod = 14;
    private final int smaSlowPeriod = 28;

    @Override
    public TradeAction onCandlestickClosed(CandlestickDto candlestickDto) {
        candlesticks.add(candlestickDto);
        double[] closedPrice = getClosedPrice(candlesticks);
        double[] smaFast = getSma(closedPrice, smaFastPeriod);
        double[] smaSlow = getSma(closedPrice, smaSlowPeriod);

        // check cross over
        if (smaFast[smaFast.length - 1] > smaSlow[smaSlow.length - 1]) {
            return TradeAction.BUY;
        }

        // check cross under
        if (smaFast[smaFast.length - 1] < smaSlow[smaSlow.length - 1]) {
            return TradeAction.SELL;
        }

        return TradeAction.DO_NOTHING;
    }

    private double[] getSma(double[] closedPrices, int smaPeriod) {
        int beginIndex = 0;
        int endIndex = candlesticks.size() - 1;
        double[] tempOutput = new double[closedPrices.length];
        double[] output = createOutputArray(closedPrices.length);

        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        RetCode retCode = taCore.ema(beginIndex, endIndex, closedPrices, smaPeriod, begin, length, tempOutput);
        if (retCode != RetCode.Success) {
            throw new RuntimeException("TA Lib error when calculate");
        }

        for (int i = smaPeriod; i < tempOutput.length; i++) {
            output[i] = tempOutput[i - begin.value];
        }

        return output;
    }

    @Override
    public void initConfig(String configPath) throws IOException {
        // for example, use hard code
        // in real code you should passed config file
        // and set properties - sma fast and slow
    }

    @Override
    public void initCandlestick(List<CandlestickDto> candlesticks) {
        logger.info("Initial candlesticks size = " + candlesticks.size());
        this.candlesticks = candlesticks;
    }
}
