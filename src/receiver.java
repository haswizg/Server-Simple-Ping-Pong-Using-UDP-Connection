import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.function.Supplier;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class receiver extends Thread {

    private final static int PORT = 2018;
    private static LinkedList<InetAddress> clintsAddress = new LinkedList<>();

    private final static Logger audit = Logger.getLogger("requests");
    private final static Logger errors = Logger.getLogger("errors");
    private final static FileHandler logfile;

    static {FileHandler file;
        try {
            file = new FileHandler("receiver.log");
        } catch (IOException e) {
            file = null;
            e.printStackTrace();
        }
        logfile = file;
        audit.addHandler(logfile);
        errors.addHandler(logfile);
    }

    private DatagramSocket socket;
    private DatagramPacket request;
    private LocalTime ReceiveTime;

    public void run() {
        String requstData = null;
        try {
            synchronized (clintsAddress){
                if(!clintsAddress.contains(request.getAddress())){
                    clintsAddress.add(request.getAddress());
                    System.out.println("requst from clint "+request.getAddress());
                }
            }
            requstData = new String(request.getData(), 0, request.getLength(), "US-ASCII");
            byte[] dataResponse = requstData.getBytes("US-ASCII");
            DatagramPacket response = new DatagramPacket(dataResponse, dataResponse.length, request.getAddress(), request.getPort());
            socket.send(response);
        } catch (RuntimeException e) {
            errors.info((Supplier<String>) e);
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            errors.info((Supplier<String>) e);
            e.printStackTrace();
        } catch (IOException e) {
            errors.info((Supplier<String>) e);
            e.printStackTrace();
        }

        audit.info(request.getAddress() + " with port " + request.getPort()+" at "+ this.ReceiveTime +" . send \"" +requstData +"\"");
    }

    public receiver(DatagramSocket incomeSocket, DatagramPacket incomeRequest) {
        this.socket = incomeSocket;
        this.request = incomeRequest;
        this.ReceiveTime=LocalTime.now();
    }

    public static void main(String[] args) {

        try {
            DatagramSocket socket = new DatagramSocket(PORT);
            try {
                while (true) {
                    DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
                    socket.receive(request);
                    new Thread(new receiver(socket, request)).start();
                }

            } catch (RuntimeException ex) {
                errors.info((Supplier<String>) ex);
                errors.log(Level.SEVERE, ex.getMessage(), ex);
            }
        } catch (IOException ex) {
            errors.info((Supplier<String>) ex);
            errors.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
