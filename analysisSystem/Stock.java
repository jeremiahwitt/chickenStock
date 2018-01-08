package analysisSystem;

import java.time.ZoneId;
import java.util.Comparator;
import java.time.LocalDate;
import java.util.Date;

public class Stock implements Comparable<Stock>
{
    private LocalDate date;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;
    private double adjustedClose;

    public Stock(LocalDate date, double open, double high, double low, double close, long volume, double adjustedClose)
    {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.adjustedClose = adjustedClose;
    }

    public LocalDate getDate() {
        return date;
    }

    public Date getUtilDate() {
        return Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public double getAdjustedClose() {
        return adjustedClose;
    }

    public void setAdjustedClose(double adjustedClose) {
        this.adjustedClose = adjustedClose;
    }

    /**
     *
     * @param otherStock
     * @return
     */
    @Override
    public int compareTo(Stock otherStock) {
        if (this.date.compareTo(otherStock.date) < 1) {
            return -1;
        } else if (this.date.compareTo(otherStock.date) > 1) {
            return 1;
        } else return 0;
    }
}
