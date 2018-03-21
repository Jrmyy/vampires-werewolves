package tcp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.net.Socket;

public class TCPClient {

    private Socket socket;
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

    public TCPClient(String host, Integer port) {
        try {
            this.socket = new Socket(host, port);
            this.out = new DataOutputStream(this.socket.getOutputStream());
            this.in = new DataInputStream(this.socket.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String listen() throws Exception {

        System.out.println("Client now listening...");

        try {

            while (!socket.isClosed() && in.available() < 1) {
                Thread.sleep(10);
            }

            if (socket.isClosed()) {
                throw new Exception("Socket closed");
            }

            byte[] order = new byte[3];

            this.in.read(order);

            String command = "";

            if (Objects.deepEquals(order, SET)) {
                command = "SET";

            } else if (Objects.deepEquals(order, HUM)) {
                command = "HUM";

            } else if (Objects.deepEquals(order, HME)) {
                command = "HME";

            } else if (Objects.deepEquals(order, MAP)) {
                command = "MAP";

            } else if (Objects.deepEquals(order, UPD)) {
                command = "UPD";

            } else if (Objects.deepEquals(order, END)) {
                command =  "END";

            } else if (Objects.deepEquals(order, BYE)) {
                command = "BYE";

            }

            System.out.println(String.format("Command received : %s ", command));
            return command;

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    public byte[] listenSET() {
        byte[] dimension = this.baseListen();
        System.out.println("    Map size : " + dimension[0] + "x" + dimension[1]);
        return dimension;
    }

    public byte[] listenHME() {
        byte[] home = this.baseListen();
        System.out.println("    Home position is : " + home[0] + "x" + home[1]);
        return home;
    }

    private byte[] baseListen() {
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
            byte[] nbHumans = new byte[1];
            byte[] humanPosition = new byte[2];
            this.in.read(nbHumans);
            System.out.println("    " + nbHumans[0] + " humans positions received");
            byte[][] allHumans = new byte[nbHumans[0]][2];
            for(int i = 0; i < nbHumans[0]; i++){
                this.in.read(humanPosition);
                System.arraycopy( humanPosition, 0, allHumans[i], 0, 2 );
                System.out.println("    Humans at position " + humanPosition[0] + "x" + humanPosition[1]);
            }
            return allHumans;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0][0];
    }

    public byte[][] listenMAP() {
        try {
            byte[] nbCells = new byte[1];
            byte[] cellContent = new byte[5];
            this.in.read(nbCells);
            System.out.println("    " + nbCells[0] + " cells to update");
            byte[][] allContents = new byte[nbCells[0]][5];
            for(int i = 0; i < nbCells[0]; i++){
                this.in.read(cellContent);
                System.arraycopy( cellContent, 0, allContents[i], 0, 5 );
                System.out.println("    Cell " + cellContent[0] + "x" + cellContent[1] + " must be updated with " + cellContent[2] + " humans, " + cellContent[3] + " vampires and " + cellContent[4] + " werewolves");
            }
            return allContents;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0][0];
    }

    public void sendName() {
        // Send the player's name to the server
        try {
            String player = "Jorubabel Le Sanglier";
            byte[] playerName = player.getBytes();
            byte nameLength = (byte) playerName.length;
            this.out.write(NME);
            this.out.write(nameLength);
            this.out.write(playerName);
            System.out.println("Name " + player + " of length " + nameLength + " has been sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMove(ArrayList<byte[]> moves) {
        // Send moves to the server
        try {
            byte nbMove = (byte) moves.size();
            this.out.write(MOV);
            this.out.write(nbMove);
            System.out.println("    " + nbMove + " moves has been sent");
            for (byte[] move: moves) {
                this.out.write(move);
                System.out.println("    Move sent : " + move[2] + " units moved from " + move[0] + "x" + move[1] + " to " + move[3] + "x" + move[4]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
