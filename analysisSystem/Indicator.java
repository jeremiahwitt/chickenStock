package analysisSystem;

import java.time.LocalDate;

/**
 * Created by Sam on 2017-04-11.
 */
public class Indicator {

    private String type; // either "buy" or "sell"
    private LocalDate date; // x on the graph, not sure what data type this is, please correct
    private double price; // y on the graph

    public Indicator(String type, LocalDate date, double price) {
        this.type = type;
        this.date = date;
        this.price = price;
    }

}
