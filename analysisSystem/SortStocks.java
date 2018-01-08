package analysisSystem;

import java.util.Collections;
import java.util.List;

public class SortStocks {

    public static List<Stock> sortByDate(List<Stock> stocks) {
        Collections.sort(stocks);

        return stocks;
    }
}
