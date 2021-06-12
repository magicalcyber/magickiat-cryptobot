package cc.magickiat.crypto.bot.strategy.impl;


import cc.magickiat.crypto.bot.constant.TradeAction;
import cc.magickiat.crypto.bot.dto.CandlestickDto;
import cc.magickiat.crypto.bot.strategy.TradeStrategy;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmaFamilyTradeStrategy implements TradeStrategy {
    private static final Logger logger = LogManager.getLogger(EmaFamilyTradeStrategy.class);

    private List<CandlestickDto> candlesticks = new ArrayList<>();
    private final Core taCore = new Core();

    private int emaPeriodFirst = 5;
    private int emaPeriodSecond = 15;
    private int emaPeriodThird = 35;
    private int emaPeriodFourth = 89;

    private BigDecimal takeProfitPercent;
    private BigDecimal stopLossPercent;

    private CandlestickDto inPositionBuy;

    private final Properties config = new Properties();

    @Override
    public TradeAction onCandlestickClosed(CandlestickDto candlestickDto) {
        candlesticks.add(candlestickDto);

        double[] closedPrices = getClosedPrice(candlesticks);
        double[] emaFirst = getEma(emaPeriodFirst, closedPrices);
        double[] emaSecond = getEma(emaPeriodSecond, closedPrices);
        double[] emaThird = getEma(emaPeriodThird, closedPrices);
        double[] emaFourth = getEma(emaPeriodFourth, closedPrices);

        TradeAction tradeAction = isInnPositionBuy(emaFirst, emaSecond, emaThird, emaFourth);
        if (tradeAction == TradeAction.BUY) {
            inPositionBuy = candlestickDto;
            return tradeAction;
        }


        tradeAction = isInPositionSell(candlestickDto.getClose(), emaFirst, emaSecond, emaThird, emaFourth);
        if (tradeAction == TradeAction.SELL) {
            inPositionBuy = null;
            return tradeAction;
        }

        return TradeAction.DO_NOTHING;
    }

    @Override
    public void initConfig(String configPath) throws IOException {
        Properties prop = new Properties();
        try (InputStream is = new FileInputStream(configPath)) {
            prop.load(is);

            config.putAll(prop);

            emaPeriodFirst = Integer.parseInt(prop.getProperty("ema.period.first"));
            emaPeriodSecond = Integer.parseInt(prop.getProperty("ema.period.second"));
            emaPeriodThird = Integer.parseInt(prop.getProperty("ema.period.third"));
            emaPeriodFourth = Integer.parseInt(prop.getProperty("ema.period.fourth"));

            takeProfitPercent = new BigDecimal(prop.getProperty("trade.take.profit.percent"));
            stopLossPercent = new BigDecimal(prop.getProperty("trade.stop.loss.percent"));

            logger.info(">>>>> Strategy Config <<<<<");
            logger.info("ema period first: {}", emaPeriodFirst);
            logger.info("ema period second: {}", emaPeriodSecond);
            logger.info("ema period third: {}", emaPeriodThird);
            logger.info("ema period fourth: {}", emaPeriodFourth);

            logger.info("take profit percent: {}", takeProfitPercent);
            logger.info("stop loss percent: {}", stopLossPercent);
        }
    }

    private TradeAction isInPositionSell(BigDecimal currentClosePrice, double[] emaFirst, double[] emaSecond, double[] emaThird, double[] emaFourth) {
        logger.debug("========= check for sale =========");

        if (inPositionBuy == null) {
            logger.debug("No in-position to check sell");
            return TradeAction.DO_NOTHING;
        }

        // ===== check circuit breaker - close under last ema =====
        if (currentClosePrice.doubleValue() < emaFourth[emaFourth.length - 1]) {
            logger.debug("current close price under last ema");
            return TradeAction.SELL;
        }

        // check last candlestick and previous is golden crossover
        int lastPosition = emaFirst.length - 1;

        logger.debug(">>> Check ordered last candlestick ema");
        boolean orderedLastEma = isOrderedEma(emaFirst[lastPosition],
                emaSecond[lastPosition],
                emaThird[lastPosition],
                emaFourth[lastPosition]);

        // ===== check circuit breaker - ema not ordered =====
        if (!orderedLastEma) {
            logger.debug("all ema not ordered");
            return TradeAction.SELL;
        }

        // ===== check circuit breaker - stop loss =====
        boolean underStopLoss = currentClosePrice.compareTo(
                inPositionBuy.getClose().subtract(inPositionBuy.getClose().multiply(stopLossPercent))
        ) < 0;

        if (underStopLoss) {
            logger.debug("STOP LOSS!");
            return TradeAction.SELL;
        }

        // ===== check take profit =====
        boolean takeProfit = currentClosePrice.compareTo(
                inPositionBuy.getClose().add(inPositionBuy.getClose().multiply(takeProfitPercent))
        ) > 0;

        if (takeProfit) {
            logger.debug("TAKE PROFIT!!!!");
            return TradeAction.SELL;
        }

        return TradeAction.DO_NOTHING;
    }

    private TradeAction isInnPositionBuy(double[] emaFirst, double[] emaSecond, double[] emaThird, double[] emaFourth) {
        logger.debug("========= check for buy =========");

        // check last candlestick and previous is golden crossover
        int lastPosition = emaFirst.length - 1;

        logger.debug(">>> Check ordered last candlestick ema");
        boolean orderedLastEma = isOrderedEma(emaFirst[lastPosition],
                emaSecond[lastPosition],
                emaThird[lastPosition],
                emaFourth[lastPosition]);

        if (!orderedLastEma) {
            return TradeAction.DO_NOTHING;
        }

        logger.debug(">>> check ordered prev last candlestick ema");
        boolean orderedPrevLastEma = isOrderedEma(emaFirst[lastPosition - 1],
                emaSecond[lastPosition - 1],
                emaThird[lastPosition - 1],
                emaFourth[lastPosition - 1]);

        if (!orderedPrevLastEma) {
            return TradeAction.BUY;
        }

        return TradeAction.DO_NOTHING;
    }

    private boolean isOrderedEma(double emaFirst, double emaSecond, double emaThird, double emaFourth) {
        logger.debug("{},{},{},{}", emaFirst, emaSecond, emaThird, emaFourth);
        return emaFirst > emaSecond &&
                emaSecond > emaThird &&
                emaThird > emaFourth;
    }

    private double[] getEma(int emaPeriod, double[] closedPrices) {
        int beginIndex = 0;
        int endIndex = candlesticks.size() - 1;
        double[] tempOutput = new double[closedPrices.length];
        double[] output = createOutputArray(closedPrices.length);

        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        RetCode retCode = taCore.ema(beginIndex, endIndex, closedPrices, emaPeriod, begin, length, tempOutput);
        if (retCode != RetCode.Success) {
            throw new RuntimeException("TA Lib error when calculate");
        }

        for (int i = emaPeriod; i < tempOutput.length; i++) {
            output[i] = tempOutput[i - begin.value];
        }

        return output;
    }

    @Override
    public void initCandlestick(List<CandlestickDto> candlesticks) {
        logger.info("Initial candlesticks size = " + candlesticks.size());
        this.candlesticks = candlesticks;
    }

}
