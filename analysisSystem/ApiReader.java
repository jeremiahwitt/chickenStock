package analysisSystem;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by pbrink on 01/04/17.
 */
public class ApiReader {

    public static boolean getCsvFromYahoo(String mnemo) {
        String url = String.format("http://ichart.finance.yahoo.com/table.csv?s=%1$s&a=01&b=01&c=1900&d=01&e=01&f=2020&g=d&ignore=.csv", mnemo);
        String filePath = "src/assets/data/FromYahooApi.csv";
        try {
            download(url, filePath);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    private static void download(String urlString, String fileToSave) throws IOException {
        URL url = new URL(urlString);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(fileToSave);
        fos.getChannel().transferFrom(rbc, 0, 5*1024*1024); // max size at 5 MB
        fos.close();
        rbc.close();
    }
}
