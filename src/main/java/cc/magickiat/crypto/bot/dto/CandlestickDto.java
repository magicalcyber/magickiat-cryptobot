package cc.magickiat.crypto.bot.dto;

import java.math.BigDecimal;
import java.util.Date;

public class CandlestickDto {
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal open;
    private BigDecimal close;
    private boolean closedCandlestick;
    private Date closeTime;

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    public boolean isClosedCandlestick() {
        return closedCandlestick;
    }

    public void setClosedCandlestick(boolean closedCandlestick) {
        this.closedCandlestick = closedCandlestick;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }

    public String toStringCvs() {
        return String.format("%s,%s,%s,%s,%s,%s",
                high,
                low,
                open,
                close,
                closedCandlestick,
                closeTime);
    }

    @Override
    public String toString() {
        return "CandlestickDto{" +
                "high=" + high +
                ", low=" + low +
                ", open=" + open +
                ", close=" + close +
                ", closedCandlestick=" + closedCandlestick +
                ", closeTime=" + closeTime +
                '}';
    }
}
