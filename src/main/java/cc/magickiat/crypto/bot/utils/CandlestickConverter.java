package cc.magickiat.crypto.bot.utils;

import cc.magickiat.crypto.bot.dto.CandlestickDto;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.Candlestick;

import java.math.BigDecimal;
import java.util.Date;

public class CandlestickConverter {

    private CandlestickConverter() {
    }

    public static CandlestickDto mapCandlestickEvent(CandlestickEvent candlestick) {
        CandlestickDto candlestickDto = new CandlestickDto();
        candlestickDto.setClose(new BigDecimal(candlestick.getClose()));
        candlestickDto.setOpen(new BigDecimal(candlestick.getOpen()));
        candlestickDto.setLow(new BigDecimal(candlestick.getLow()));
        candlestickDto.setHigh(new BigDecimal(candlestick.getHigh()));
        candlestickDto.setClosedCandlestick(candlestick.getBarFinal());
        candlestickDto.setCloseTime(new Date(candlestick.getCloseTime()));
        return candlestickDto;
    }

    public static CandlestickDto mapCandlestick(Candlestick candlestick) {
        CandlestickDto candlestickDto = new CandlestickDto();
        candlestickDto.setClose(new BigDecimal(candlestick.getClose()));
        candlestickDto.setOpen(new BigDecimal(candlestick.getOpen()));
        candlestickDto.setLow(new BigDecimal(candlestick.getLow()));
        candlestickDto.setHigh(new BigDecimal(candlestick.getHigh()));
        candlestickDto.setCloseTime(new Date(candlestick.getCloseTime()));
        return candlestickDto;
    }
}
