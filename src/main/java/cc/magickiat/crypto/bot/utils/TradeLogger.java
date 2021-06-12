package cc.magickiat.crypto.bot.utils;

import cc.magickiat.crypto.bot.constant.TradeAction;
import cc.magickiat.crypto.bot.dto.CandlestickDto;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class TradeLogger {
    private final PrintWriter writer;

    public TradeLogger() throws IOException {
        File file = new File("trade.log");
        if (!file.exists()) {
            file.createNewFile();
        }
        writer = new PrintWriter(file);
    }

    public void writeLog(TradeAction action, String symbol, CandlestickDto candlestick) {
        writer.println(String.format("%s,%s,%s", action.toString(), symbol, candlestick.toStringCvs()));
        writer.flush();
    }

    public void close() {
        writer.close();
    }
}
