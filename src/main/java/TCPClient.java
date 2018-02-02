import java.io.*;
import java.util.Objects;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import static java.lang.Thread.sleep;

public class TCPClient {

    private String host;
    private Integer port;
    private Socket socket;
    private String player = "Jorubabel Le Sanglier";
    private DataOutputStream out;
    private DataInputStream in;
    private static byte[]
            SET = { 'S', 'E', 'T' },
            HUM = { 'H', 'U', 'M' },
            HME = { 'H', 'M', 'E' },
            MAP = { 'M', 'A', 'P' },
            UPD = { 'U', 'P', 'D' },
            END = { 'E', 'N', 'D' },
            BYE = { 'B', 'Y', 'E' },
            NME = { 'N', 'M', 'E' },
            MOV = { 'M', 'O', 'V' };

    public TCPClient() {
        Properties prop = new Properties();
        FileInputStream input = null;
        try {
            System.out.println("Loading properties...");
            //input = new FileInputStream("config.properties");
            //prop.load(input);

            //this.setHost(prop.getProperty("host"));
            //this.setPort(Integer.parseInt(prop.getProperty("port")));

            this.setHost("localhost");
            this.setPort(1234);

            this.socket = new Socket("127.0.0.1", 5555);
            this.out = new DataOutputStream(this.socket.getOutputStream());
            this.in = new DataInputStream(this.socket.getInputStream());

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String listen() {
        System.out.println("Client now listening...");
        try {
            while (!socket.isClosed() && in.available() < 1)
                Thread.sleep(10);
            if (socket.isClosed())
                throw new Exception("Socket closed");
            byte[] order = new byte[3];
            this.in.read(order);
            if (Objects.deepEquals(order,SET)) {
                System.out.println("SET");
                return "SET";
            } else if (Objects.deepEquals(order,HUM)) {
                System.out.println("HUM");
                return "HUM";
            } else if (Objects.deepEquals(order,HME)) {
                System.out.println("HME");
                return "HME";
            } else if (Objects.deepEquals(order,MAP)) {
                System.out.println("MAP");
                return "MAP";
            } else if (Objects.deepEquals(order,UPD)) {
                System.out.println("UPD");
                return "UPD";
            } else if (Objects.deepEquals(order,END)) {
                System.out.println("END");
                return "END";
            } else if (Objects.deepEquals(order,BYE)) {
                System.out.println("BYE");
                return "BYE";
            } else {
                System.out.println("Unable to read order...");
                return "";
            }
        }  catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public byte[] listenSET() {
        try {
            byte[] dimensions = new byte[2];
            this.in.read(dimensions);
            return dimensions;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public byte[][] listenHUM() {
        try {
            byte[] nbHouses = new byte[1];
            byte[] coordHouse = new byte[2];
            this.in.read(nbHouses);
            byte[][] allHouses = new byte[nbHouses[0]][2];
            for(int i=0; i<nbHouses[0]; i++){
                this.in.read(coordHouse);
                allHouses[i] = coordHouse;
            }
            return allHouses;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0][0];
    }

    public byte[] listenHME() {
        try {
            byte[] coordHome = new byte[2];
            this.in.read(coordHome);
            return coordHome;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public byte[][] listenMAP() {
        try {
            byte[] nbCells = new byte[1];
            byte[] cellContent = new byte[5];
            this.in.read(nbCells);
            byte[][] allContents = new byte[nbCells[0]][5];
            for(int i=0; i<nbCells[0]; i++){
                this.in.read(cellContent);
                System.arraycopy( cellContent, 0, allContents[i], 0, 5 );
            }
            return allContents;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0][0];
    }

    public void sendName() {
        //Send the player's name to the server
        try {
            byte[] playerName = this.player.getBytes();
            byte nameLength = (byte) playerName.length;
            this.out.write(NME);
            this.out.write(nameLength);
            this.out.write(playerName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMove(byte[][] move) {
        //Send move to the server
        try {
            byte nbMove = (byte) move.length;
            this.out.write(MOV);
            this.out.write(nbMove);
            for(int i=0; i<nbMove; i++){
                this.out.write(move[i]);
            }
        } catch (IOException e) {
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
