package analysisSystem;

import accountManagement.AccountController;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import main.NonSystemController;
import main.SystemController;

import java.io.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class GraphController extends NonSystemController {

    private int currentPlotID = 0;
    private String currentStockMnemo = "Default";
    private String mnemoFilePath = "src/assets/data/LastMnemo.txt";
    //May be changed later
    private List<Stock> stockDataList;
    private List<Stock> khatibList;
    private List<Stock>[] plotDataLists = new List[5];

    private static String[] stockNames = {
            "3M", "American Express", "Apple", "Boeing",
            "Caterpillar", "Chevron", "Cisco Systems", "Coca-Cola",
            "DuPont", "ExxonMobil", "General Electric", "Goldman Sachs",
            "The Home Depot", "IBM", "Intel", "Johnson & Johnson", "JPMorgan Chase",
            "McDonald's", "Merck", "Microsoft", "Nike", "Pfizer", "Procter & Gamble",
            "Travelers", "UnitedHealth Group", "United Technologies", "Verizon", "Visa",
            "Wal-Mart", "Walt Disney"};
    
    private static String[] stockIDs = {"MMM", "AXP", "AAPL", "BA", "CAT", "CVX", "CSCO", "KO", "DD", "XOM", "GE"
    		, "GS", "HD", "IBM", "INTC", "JNJ", "JPM", "MCD", "MRK", "MSFT", "NKE", 
    		"PFE", "PG", "TRV", "UNH", "UTX", "VZ", "V", "WMT", "DIS"};

    private enum Plots {STOCK, MA20,MA50,MA100,MA200};
    private int[] plotID = {-2,-2,-2,-2,-2};
    private int[] numberMonth = {31,28,31,30,31,30,31,31,30,31,30,31};
    private double totalDaysYear = 365;
    private int minYear = 2025;
    private int maxYear = 2012;
    private XYChart.Series series;
    private XYChart.Series buySer;
    private XYChart.Series sellSer;

    private double[] originalClose;

    private int plotTracker;
    private int currentMaDay;
    /* Private Static Variables */
    /**
     * Private 'Instance' Variable, ensuring existence of only one Controller
     */
    private static GraphController graphControllerInstance = null;

    /* FXML OBJECTS */

    /**
     * stockGraph LineChart object in FXML
     */
    // NOTE: Data types can be changed, just must stay objects :)
    @FXML
    private LineChart<Double, Double> stockGraph;
    @FXML
    private ScatterChart<Double, Double> buyGraph;

    @FXML
    private ScatterChart<Double, Double> sellGraph;

    /* AXIS SELECTORS */
    @FXML
    private NumberAxis xAxis;

    @FXML
    private NumberAxis yAxis;

   @FXML
   private NumberAxis xAxisBuy;

    @FXML
  private NumberAxis yAxisBuy;

    @FXML
    private NumberAxis xAxisSell;

    @FXML
    private NumberAxis yAxisSell;

    /* MOVING AVERAGE SELECTORS */
    @FXML
    private CheckMenuItem movAvg20d;

    @FXML
    private CheckMenuItem movAvg50d;

    @FXML
    private CheckMenuItem movAvg100d;

    @FXML
    private CheckMenuItem movAvg200d;

    /* TIMEFRAME SELECTORS */
    @FXML
    private RadioMenuItem timeFrame1Year;

    @FXML
    private RadioMenuItem timeFrame2Years;

    @FXML
    private RadioMenuItem timeFrame5Years;

    @FXML
    private RadioMenuItem timeFrameAllYears;

    @FXML
    private Pane buySellIndicator;

    /* STOCK MENU */

    @FXML
    private Menu stockMenu;

    /* CONSTRUCTORS */

    /**
     * Private Constructor so that the GraphController can only be accessed
     * via getInstance()
     */
    private GraphController(){
        currentPlotID = 0;
    }

    /**
     * Allows for the System to access the GraphController. Uses lazy instantiation
     * to ensure that the GraphController is only initiated once it is required
     * @return
     */
    public static GraphController getInstance() {
        if(graphControllerInstance == null){
            graphControllerInstance = new GraphController();
        }
        return graphControllerInstance;
    }

    /* GRAPH MODIFICATION */

    /**
     * Allows for the graph to be setup when the View is loaded.
     */
    // NOTE: Feel free to completely change and/or remove this to suit your needs!
    public void setupGraph()
    {
        removeAllDataSeries();
        stockGraph.setCreateSymbols(false);

        xAxis.setTickLabelFormatter(new NumberStringConverter(NumberFormat.getInstance(Locale.FRENCH)));
        xAxisBuy.setTickLabelFormatter(new NumberStringConverter(NumberFormat.getInstance(Locale.FRENCH)));
        xAxisSell.setTickLabelFormatter(new NumberStringConverter(NumberFormat.getInstance(Locale.FRENCH)));

        stockGraph.getYAxis().setLabel("Closing Price");
        buyGraph.getYAxis().setLabel("Closing Price");
        sellGraph.getYAxis().setLabel("Closing Price");

        stockGraph.getXAxis().setLabel("Time");
        buyGraph.getXAxis().setLabel("Time");
        sellGraph.getXAxis().setLabel("Time");

        stockGraph.setTitle(currentStockMnemo);
        buyGraph.setTitle(currentStockMnemo);
        sellGraph.setTitle(currentStockMnemo);
        khatibList=populateList();
        originalClose=new double[khatibList.size()];
        for(int x=0; x<originalClose.length; x++)
        {
            originalClose[x]=khatibList.get(x).getClose();
        }
        setData(khatibList);
        addDataSeries(stockDataList, currentStockMnemo, Plots.STOCK);

        stockGraph.setCreateSymbols(false);
    }

    /**
     *  Function to set current stock data list and stock title
     */
    public void setData(List dataList)
    {
        stockGraph.setTitle(currentStockMnemo + " Stock Analysis");
        stockDataList = dataList;

        plotDataLists[0] = stockDataList;
    }

    /**
     * Function to be used to add the data series to the graph
     */
    public void addDataSeries(List<Stock> list, String seriesName, Plots plot)
    {
        //create sereis for actual data / moving average
        series = new XYChart.Series();
        series.setName(seriesName);

        //two seresis for buy and sell indicators
        buySer = new XYChart.Series();
        buySer.setName(seriesName);
        sellSer = new XYChart.Series();
        sellSer.setName(seriesName);

        Stock currentStock;
        boolean maAbovePrice;

        //data list for actual price or moving average
        List<XYChart.Data> dataList = new ArrayList<>();

        //create data lists for buy and sell indicators
        List<XYChart.Data> buyData = new ArrayList();
        List<XYChart.Data> sellData = new ArrayList();

        //stock list ma is operaton on as moving average
        List<Stock> ma = list;
        //stock list actual is operated on an actual price list
        List<Stock> actual = khatibList;

        //copy all the moving average and actual price values into primitve arrays
        double[] maArray = new double[ma.size()];
        double[] actArray = new double[ma.size()];
        for(int x=0; x<ma.size(); x++)
        {
            maArray[x] = ma.get(x).getClose();
            actArray[x] = actual.get(x).getClose();
        }

        //console test
        System.out.println("MA Size: "+ma.size());
        System.out.println("Actual: "+actual.size());

        //check MA and actual price relative position for start day
        if(ma.get(0).getClose()>actual.get(0).getClose())
        {
            maAbovePrice=true;
        }
        else
        {
            maAbovePrice=false;
        }

        if(ma==null)
        {
            System.out.println("GOT NULL");
        }


        int counter=0;
        int test = 0;
        while(counter<list.size())
        {

            currentStock=list.get(counter);

            LocalDate date = currentStock.getDate();
            double d = convertDateToDouble(Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));


            dataList.add(new XYChart.Data(d, currentStock.getClose()));
            if((counter>30) && (counter<70))
            {
                System.out.print(originalClose[counter] + " " + maArray[counter] + " " + maAbovePrice);
                System.out.println();
            }

            if ((originalClose[counter] > maArray[counter]) && (maAbovePrice == true))
            {
                maAbovePrice = false;
                //setup a buy trigger
                buyData.add(new XYChart.Data(d, currentStock.getClose()+20));
                //console test
                System.out.println("Breaks Up");
            }
            //moving average falles below price then sell
            if ((originalClose[counter] < maArray[counter]) && (maAbovePrice == false))
            {
                maAbovePrice = true;
                //setup a sell trugger
                sellData.add(new XYChart.Data(d, currentStock.getClose()-10));
                //console test
                System.out.println("Breaks Down");
            }
            counter++;
        }
        
        xAxis.setAutoRanging(false);
        xAxisBuy.setAutoRanging(false);
        xAxisSell.setAutoRanging(false);
        if(plot == Plots.STOCK) {
            xAxis.setLowerBound(minYear - 1);
           xAxisBuy.setLowerBound(minYear-1);
            xAxisSell.setLowerBound(minYear-1);
           xAxis.setUpperBound(maxYear);
           xAxisBuy.setUpperBound(maxYear);
            xAxisSell.setUpperBound(maxYear);
        }
        xAxis.setTickUnit((maxYear-minYear)/6);
        xAxisBuy.setTickUnit((maxYear-minYear)/6);
        xAxisSell.setTickUnit((maxYear-minYear)/6);

        series.getData().addAll(dataList);
        buySer.getData().addAll(buyData);
        sellSer.getData().addAll(sellData);
        addSeriesID(plot);

        buyGraph.getData().add(plotID[getEnumID(plot)], buySer);
        sellGraph.getData().add(plotID[getEnumID(plot)], sellSer);
        stockGraph.getData().add(plotID[getEnumID(plot)],series);
    }

    /**
     * Function that converts the Dates into double values for display on the graph
     * using the formula date = year + (day+sum(daysOfMonthsBeforeCurrent))/totalDaysYear
     */
    private double convertDateToDouble(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int y = cal.get(Calendar.YEAR), m=cal.get(Calendar.MONTH)+1, d = cal.get(Calendar.DAY_OF_MONTH);
        if(minYear > y)
            minYear = y;
        if(y > maxYear) {
            maxYear = y;
        }

        double dd = y + getTotalDays(m,d)/totalDaysYear;
       // double roundDd = Math.round(dd*10000.0)/ 10000.0;
        return dd;
    }

    /**
     * Helper Function that calculates the total number of days that have passed in the year
     * based on the month and day
     */
    private int getTotalDays(int m, int d)
    {
        int totalDays = d;

        for(int i = 1; i < m;i++)
        {
            totalDays+=numberMonth[i-1];
        }

        return totalDays;
    }

    /**
     * Helper function that returns the index of a Plot enum value
     */
    private int getEnumID(Plots plot)
    {
        int i =0;
        switch(plot)
        {
            case STOCK:
                i = 0;
                break;
            case MA20:
                i = 1;
                break;
            case MA50:
                i = 2;
                break;
            case MA100:
                i = 3;
                break;
            case MA200:
                i = 4;
                break;
        }
        return i;
    }
    private int getDays(Plots plot)
    {
        int i =0;
        switch(plot)
        {
            case MA20:
                return 20;

            case MA50:
                return 50;

            case MA100:
                return 100;

            case MA200:
                return 200;
        }
        return -1;
    }

    /**
     * Helper function that returns the index of a data list based on the days of the
     * moving average
     */
    private int getDataID(int days)
    {
        int i =0;
        switch(days)
        {
            case 20:
                i = 1;
                break;
            case 50:
                i = 2;
                break;
            case 100:
                i = 3;
                break;
            case 200:
                i = 4;
                break;
        }
        return i;
    }

    /**
     *  Helper Function that sets the series ID of the plot in the plotID array
     * @param plot
     */
    private void addSeriesID(Plots plot)
    {
        int i = getEnumID(plot);
        plotID[i] = currentPlotID;
        currentPlotID++;
    }

    /**
     * Function that removes all data from the graph and resets the values of currentPlotID
     * and removes all data lists
     */
    public void removeAllDataSeries()
    {
        currentPlotID =0;
        stockGraph.getData().clear();
        buyGraph.getData().clear();
        sellGraph.getData().clear();
        for(int i = 0; i<plotDataLists.length;i++)
        {
            plotDataLists[i] = null;
        }
    }

    /**
     * Function that removes a series from the graph based on the Plot enum given
     * @param plot
     */
    public void removeDataSeries(Plots plot) {
        int i = getEnumID(plot);
        stockGraph.getData().remove(plotID[i]);
        buyGraph.getData().remove(plotID[i]);
        sellGraph.getData().remove(plotID[i]);
        updatePlotIDs(i);
        currentPlotID--;
        if (currentPlotID <= 1) {
            setBuySellLegendVisible(false);
        }
    }

    /**
     * Temporary function to show all plotIDs for testing purposes
     */
    private void displayPlotIDs()
    {
        for(int j = 0; j<plotID.length;j++)
        {
            System.out.print(plotID[j] + " ");
        }
        System.out.println();
    }

    /**
     * Utility Function that updates the plotID of the plots after removal
     * @param i
     */
    private void updatePlotIDs(int i)
    {
        plotID[i] = -2;
        for(int j = 1; j<plotID.length;j++)
        {
            if(plotID[j]!=-2)
                plotID[j]--;
            if(plotID[j] == 0)
                plotID[j] = 1;
        }
    }

    /**
     * Function used to retrieve the data from a csv file
     * @return a list of Stock objects
     */
    public List<Stock> populateList()
    {
        if (currentStockMnemo == "Default") {
            currentStockMnemo = getLastMnemo();
        }

        String filePath = "src/assets/data/FromYahooApi.csv";
        File csvFile = new File(filePath);
        if (csvFile.exists() && !csvFile.isDirectory()) {
            return CsvReader.readCSV(filePath, ",", true);
        } else {
            currentStockMnemo = "MMM";
            loadStockData(currentStockMnemo);
            removeAllDataSeries();
            return CsvReader.readCSV(filePath, ",", true);
        }

    }

    public String getLastMnemo() {
        File mnemoFile = new File(mnemoFilePath);
        String lastMnemo = "MMM";
        if (mnemoFile.exists() && !mnemoFile.isDirectory()) {
            try (BufferedReader br = new BufferedReader(new FileReader(mnemoFilePath))) {
                String line;
                if ((line = br.readLine()) != null) {
                    lastMnemo = line;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            setLastMnemo(lastMnemo);
        }
        return lastMnemo;
    }

    public void setLastMnemo(String mnemo) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(mnemoFilePath);
            bw = new BufferedWriter(fw);
            bw.write(mnemo);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /* MOVING AVERAGE MODIFICATION */

    /**
     * Function that calculates the moving average for a set time interval
     * @param days  integer value for selected time interval
     * @return List of Stock objects
     */
    public List<Stock> calculateMovingAverage(int days)
    {
        List<Stock> stockData = new ArrayList<>(plotDataLists[0]);
        List<Stock> movingAvData = MovingAverage.calculateAllData(stockData, days);
        Collections.reverse(movingAvData);
        plotDataLists[getDataID(days)] = movingAvData;
        plotTracker = getDataID(days);
        currentMaDay=days;
        return movingAvData;
    }
    
        @FXML
        public void displayMovAvg20Days(ActionEvent event) {
            // TODO Add selection logic
            if(movAvg20d.isSelected())
            {
                addDataSeries(calculateMovingAverage(20), "Moving Average 20 Days", Plots.MA20);
                setBuySellLegendVisible(true);
            }
            else if(!movAvg20d.isSelected())
            {
                removeDataSeries(Plots.MA20);
            }
        }

        @FXML
        public void displayMovAvg50Days(ActionEvent event){
            if(movAvg50d.isSelected())
            {
                addDataSeries(calculateMovingAverage(50), "Moving Average 50 Days", Plots.MA50);
                setBuySellLegendVisible(true);
            }
            else if(!movAvg50d.isSelected())
            {
                removeDataSeries(Plots.MA50);
            }
        }

        @FXML
        public void displayMovAvg100Days(ActionEvent event){
            if(movAvg100d.isSelected())
            {
                addDataSeries(calculateMovingAverage(100), "Moving Average 100 Days", Plots.MA100);
                setBuySellLegendVisible(true);
            }
            else if(!movAvg100d.isSelected())
            {
                removeDataSeries(Plots.MA100);
            }
        }

        @FXML
        public void displayMovAvg200Days(ActionEvent event){
            // TODO Add selection logic
            if(movAvg200d.isSelected())
            {

                addDataSeries(calculateMovingAverage(200), "Moving Average 200 Days", Plots.MA200);
                setBuySellLegendVisible(true);
            }
            else if(!movAvg200d.isSelected())
            {
                removeDataSeries(Plots.MA200);
            }
        }

    /* TIME FRAME MODIFICIATION */
        public void setYAxisBoundaries(int i)
        {
            double maxVal = 2;
            double minVal = 900;
            Calendar cal = Calendar.getInstance();
            int numYears;
            int targetMinYear = maxYear-i;
            int y;
            List<Stock> stockData = plotDataLists[0];
            Iterator<Stock> itr = stockData.iterator();
            while(itr.hasNext()) {
                boolean done = false;
                numYears = i;
                Stock st = itr.next();
                Date date = st.getUtilDate();
                double val = st.getClose();
                cal.setTime(date);
                y = cal.get(Calendar.YEAR);
                System.out.println(y + " " + numYears + " " + targetMinYear);
                while (numYears >= 0) {
                    if (y > targetMinYear)
                    {
                        if(y == maxYear - numYears) {
                            System.out.print(y + " " + val);
                            if (val < minVal) {
                                minVal = val;
                            }
                            if (val > maxVal) {
                                maxVal = val;
                            }
                        }
                    } else {
                        done = true;
                        break;
                    }
                    numYears--;
                }
                if (done)
                    break;
            }

            System.out.println(maxVal+ "" + minVal);
            yAxis.setAutoRanging(false);
           yAxisBuy.setAutoRanging(false);
            yAxisSell.setAutoRanging(false);
            yAxis.setLowerBound(Math.round(minVal-50));
           yAxisBuy.setLowerBound(Math.round(minVal-50));
            yAxisSell.setLowerBound(Math.round(minVal-50));
            yAxis.setUpperBound(Math.round(maxVal+50));
           yAxisBuy.setUpperBound(Math.round(maxVal+50));
            yAxisSell.setUpperBound(Math.round(maxVal+50));

        }

        @FXML
        public void changeTimeFrame1Year(ActionEvent event)
        {
            // TODO Add selection logic
            xAxis.setLowerBound(maxYear - 1);
           xAxisBuy.setLowerBound(maxYear-1);
            xAxisSell.setLowerBound(maxYear-1);
            setYAxisBoundaries(1);

        }

        @FXML
        public void changeTimeFrame2Years(ActionEvent event){
            // TODO Add selection logic
            xAxis.setLowerBound(maxYear-2);
           xAxisBuy.setLowerBound(maxYear-2);
            xAxisSell.setLowerBound(maxYear-2);
            setYAxisBoundaries(2);
        }

        @FXML
        public void changeTimeFrame5Years(ActionEvent event){
            // TODO Add selection logic
            xAxis.setLowerBound(maxYear-5);
          xAxisBuy.setLowerBound(maxYear-5);
            xAxisSell.setLowerBound(maxYear-5);
            setYAxisBoundaries(5);
        }

        @FXML
        public void changeTimeFrameAllData(ActionEvent event){
            // TODO Add selection logic
            xAxis.setLowerBound(minYear-1);
          xAxisBuy.setLowerBound(minYear-1);
            xAxisSell.setLowerBound(minYear-1);
            yAxis.setAutoRanging(true);
           yAxisBuy.setAutoRanging(true);
            yAxisSell.setAutoRanging(true);
        }

        /**
         * the following selects the stock from most viewed list
         * @param event
         */
        @FXML
        public void selectMV(ActionEvent event)
        {
        	
        	//Menu actual = (Menu)scene.lookup("#"+src.getText());
        	MenuItem src = (MenuItem)event.getSource();
        	if(src.getText().equals("none"))
        		return;
        	int x=0;
        	for(x=0; x<stockNames.length; x++)
        	{
        		if(stockNames[x].equals(src.getText()))
        		{
        			break;
        		}
        	}
        	
            currentStockMnemo = stockIDs[x];
            loadStockData(stockIDs[x]);

         
            //khatibs code for fav stock
            AccountController.getInstance().clickedStock(src.getText());


            // reset the timeframe view
            if (timeFrame1Year.isSelected()) {
                changeTimeFrame1Year(event);
            } else if (timeFrame2Years.isSelected()) {
                changeTimeFrame2Years(event);
            } else if (timeFrame5Years.isSelected()) {
                changeTimeFrame5Years(event);
            } else if (timeFrameAllYears.isSelected()) {
                changeTimeFrameAllData(event);
            }

            // reset the moving averages
            if (movAvg20d.isSelected()) {
                displayMovAvg20Days(event);
            } if (movAvg50d.isSelected()) {
                displayMovAvg50Days(event);
            } if (movAvg100d.isSelected()) {
                displayMovAvg100Days(event);
            } if (movAvg200d.isSelected()) {
                displayMovAvg200Days(event);
            }
        	
        }

        public void setBuySellLegendVisible(boolean makeVisible) {
            if (makeVisible) {
                buySellIndicator.setOpacity(1);
            } else {
                buySellIndicator.setOpacity(0);
            }
        }

        /**
         * the following funcction is triggerd when most vewied tab is entered
         */
        @FXML
        public void setMV(Event event) 
        {
        	//get the menu and its items
        	Menu src = (Menu) event.getSource();
        	List<MenuItem> itms = src.getItems();
        	Iterator<MenuItem> it = itms.iterator();
        	
        	//get the three most fav for this user
        	List<String> mv = AccountController.getInstance().getMostViewed();
        	Iterator<String> stc = mv.iterator(); 
        	
        	//set the text for the three most fav
        	while(stc.hasNext())
        	{
        		MenuItem item = it.next();
        		String val = stc.next();
        		System.out.println("Value: "+val);
        		int x=0;
        		for(x=0;x<val.length();x++)
        		{
        			if(val.charAt(x)==':')
        				break;
        		}
        		item.setText(val.substring(x+1, val.length()));
        	}
        	//System.out.println("Old account");
        }
/*        @FXML
        public void setMV()
        {
        	System.out.println("New account");
        }
        */
        

    /**
     * Allows the user to select a specific stock to display.
     * The method grabs the Stock Symbol associated with that event, and then loads the data for that stock.
     * If need be, the text from the menu button can be grabbed to generate the Graph title. Will make sure
     * all states remain as they were previously.
     * @param event
     */
    @FXML
    public void selectStock(ActionEvent event){
        MenuItem source = (MenuItem) event.getSource();
        String stockSymbol = source.getId();
        currentStockMnemo = stockSymbol;
        loadStockData(stockSymbol);

        //notify account controller a stock was clicked
        AccountController.getInstance().clickedStock(source.getText());

        // reset the timeframe view
        if (timeFrame1Year.isSelected()) {
            changeTimeFrame1Year(event);
        } else if (timeFrame2Years.isSelected()) {
            changeTimeFrame2Years(event);
        } else if (timeFrame5Years.isSelected()) {
            changeTimeFrame5Years(event);
        } else if (timeFrameAllYears.isSelected()) {
            changeTimeFrameAllData(event);
        }

        // reset the moving averages
        if (movAvg20d.isSelected()) {
            displayMovAvg20Days(event);
        } if (movAvg50d.isSelected()) {
            displayMovAvg50Days(event);
        } if (movAvg100d.isSelected()) {
            displayMovAvg100Days(event);
        } if (movAvg200d.isSelected()) {
            displayMovAvg200Days(event);
        }

        setLastMnemo(currentStockMnemo);
    }

    private void loadStockData(String stockSymbol) {
        ApiReader.getCsvFromYahoo(stockSymbol);
        setupGraph();
    }

    @FXML
    public void launchDeleteAccountView(ActionEvent event){
        AccountController.getInstance().launchDeleteAccountView(event);
    }

    /**
     * Used to log the user out of the system. Kills the current stage and GraphController. Returns
     * the user to the login screen
     * @param event
     */
    public void logout(ActionEvent event){
            Stage stage  = (Stage) stockGraph.getScene().getWindow();
            stage.close();
            Stage newStage = new Stage();
            SystemController.setCurrentStage(newStage);
            graphControllerInstance = new GraphController();
            SystemController.getInstance().logout(event);
        }

}
