package analysisSystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CsvReader {

    public static List<Stock> readCSV(String path, String splitChar, boolean skipFirstLine) {
        String line = "";
        List<Stock> stocks = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            while ((line = br.readLine()) != null) {

                if (skipFirstLine) {
                    line = br.readLine();
                    skipFirstLine = false;
                }

                // use comma as separator
                String[] stockString = line.split(splitChar);

                // make sure that there are the required number of parts
                if (stockString.length != 7) {
                    System.err.println("Invalid stock entered: " + line);
                } else {
                    // make sure that all parsing happens properly
                    try {
                        Stock stock = new Stock(
                                LocalDate.parse(stockString[0], DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.CANADA)),
                                Double.parseDouble(stockString[1]),
                                Double.parseDouble(stockString[2]),
                                Double.parseDouble(stockString[3]),
                                Double.parseDouble(stockString[4]),
                                Long.parseLong(stockString[5]),
                                Double.parseDouble(stockString[6])
                        );
                        stocks.add(stock);
                    } catch (Exception e) {
                        System.err.println("Malformed CSV file provided: " + path);
                        e.printStackTrace();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return stocks;
    }

}
