package analysisSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 * A collection of methods to calculate the moving averages
 */
public class MovingAverage {

    /**
     * Calculate the averages for all data in the stocks
     * @param stocks A list of stocks
     * @param days The number of days the moving average should be based off of
     * @return A new sorted list of averaged stocks
     */
	//added by khatib to track closings
	//public static List<Double> closePrices;
	
    public static List<Stock> calculateAllData(List<Stock> stocks, int days) {
    	//closePrices = new ArrayList<Double>();
        if (stocks.size() == 0) return stocks;
        Collections.sort(stocks);
        List<Stock> maStocks = new ArrayList<>(stocks);

        // lists for all data in the average
        List<Double> avOpenList = new ArrayList<Double>();
        List<Double> avHighList = new ArrayList<Double>();
        List<Double> avLowList = new ArrayList<Double>();
        List<Double> avCloseList = new ArrayList<Double>();
        List<Long> avVolumeList = new ArrayList<Long>();
        List<Double> avAdjustedCloseList = new ArrayList<Double>();

        // this assumes that all dates are present
        for (int i = 0; i < stocks.size(); i++) {
            if (avOpenList.size() < days) {
                avOpenList.add(stocks.get(i).getOpen());
                avHighList.add(stocks.get(i).getHigh());
                avLowList.add(stocks.get(i).getLow());
                avCloseList.add(stocks.get(i).getClose());
                //added by khatib to hijack the close price
                //closePrices.add(stocks.get(i).getClose());
                avVolumeList.add(stocks.get(i).getVolume());
                avAdjustedCloseList.add(stocks.get(i).getAdjustedClose());
            } else {
                avOpenList.remove(0);
                avHighList.remove(0);
                avLowList.remove(0);
                avCloseList.remove(0);
                avVolumeList.remove(0);
                avAdjustedCloseList.remove(0);
                avOpenList.add(stocks.get(i).getOpen());
                avHighList.add(stocks.get(i).getHigh());
                avLowList.add(stocks.get(i).getLow());
                avCloseList.add(stocks.get(i).getClose());
                //added by khatib to hijack the close price
                //closePrices.add(stocks.get(i).getClose());
                avVolumeList.add(stocks.get(i).getVolume());
                avAdjustedCloseList.add(stocks.get(i).getAdjustedClose());
            }

            // compute and set averages
            double sumOpen = 0;
            for (Double av : avOpenList) {
                sumOpen += av;
            }
            maStocks.get(i).setOpen(sumOpen/(avOpenList.size()));

            double sumHigh = 0;
            for (Double av : avHighList) {
                sumHigh += av;
            }
            maStocks.get(i).setHigh(sumHigh/(avHighList.size()));

            double sumLow = 0;
            for (Double av : avLowList) {
                sumLow += av;
            }
            maStocks.get(i).setLow(sumLow/(avLowList.size()));

            double sumClose = 0;
            for (Double av : avCloseList) {
                sumClose += av;
            }
            maStocks.get(i).setClose(sumClose/(avCloseList.size()));

            long sumVolume = 0;
            for (Long av : avVolumeList) {
                sumVolume += av;
            }
            maStocks.get(i).setVolume(sumVolume/(avVolumeList.size()));

            double sumAdjustedClose = 0;
            for (Double av : avAdjustedCloseList) {
                sumAdjustedClose += av;
            }
            maStocks.get(i).setAdjustedClose(sumAdjustedClose/(avAdjustedCloseList.size()));
        }
        return maStocks;
    }

    // method was implemented to calculate buy and sell indicators.
    public static List<Indicator> getIndicators(List<Stock> stockData) {
        // CHANGE THIS TO WHATEVER MOVING AVERAGE WE ARE SUPPOSED TO COMPARE THE STOCK PRICE TO
        // when stock price crosses 50 day moving average
        List<Stock> movingAverageData = calculateAllData(stockData, 50);
        List<Indicator> indicatorList = new ArrayList<>();

        boolean stockBelowMovingAverage = false; // need in define to avoid syntax error. makes no actual difference though
        boolean tmpStockBelowMovingAverage;

        // here's my algo: we can check if the price is above or below the price of the moving average. If it's different that the previous iteration, it crossed
        // if it crossed, create an indicator and put it into the list
        for (int i = 0; i < movingAverageData.size(); i++) {

            // first day, just set stockBelowMovingAverage
            if (i == 0) {
                stockBelowMovingAverage = (stockData.get(i).getClose() < movingAverageData.get(i).getClose());
                continue;
            }

            // all other days. check if the stock is below the moving average
            tmpStockBelowMovingAverage = (stockData.get(i).getClose() < movingAverageData.get(i).getClose());

            if (tmpStockBelowMovingAverage != stockBelowMovingAverage) { // cross over has occured
                if (stockBelowMovingAverage) { // stock was below moving average, now is above, BUY INDICATOR
                    // BUY INDICATOR. this creates a buy indicator at the price and date of the cross
                    Indicator tmpIndicator = new Indicator("buy", stockData.get(i).getDate(), stockData.get(i).getClose());
                    indicatorList.add(tmpIndicator);
                } else {
                    // SELL INDICATOR. same as above except sell indicator
                    Indicator tmpIndicator = new Indicator("sell", stockData.get(i).getDate(), stockData.get(i).getClose());
                    indicatorList.add(tmpIndicator);
                }
            }
        }

        // return indicator list
        return indicatorList;
    }

}
