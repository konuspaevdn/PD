import javax.xml.crypto.Data;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {

    private static HttpURLConnection connect(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "text/plain");
        con.setDoOutput(true);
        return con;
    }

    public static void main(String[] args) throws IOException {
        HttpURLConnection con = null;
        DataOutputStream out = null;
        BufferedReader in = null;
        String str = "";
        try {
            con = connect(args[0]);
            out = new DataOutputStream(con.getOutputStream());
            out.write(str.getBytes());
            in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            for (int c; (c = in.read()) >= 0;)
                System.out.print((char)c);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(99);
        } finally {
            if (con != null) {
                con.disconnect();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }
}