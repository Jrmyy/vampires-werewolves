import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import static java.lang.Thread.sleep;

public class TCPClient {

    private String host;
    private Integer port;

    public TCPClient() {
        Properties prop = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            prop.load(input);

            this.setHost(prop.getProperty("host"));
            this.setPort(Integer.parseInt(prop.getProperty("port")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        System.out.println("toto");
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }



}
