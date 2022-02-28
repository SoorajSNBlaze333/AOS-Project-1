package src;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Server implements Runnable {
  public ServerSocket serverSocket = null;
  public Socket socket = null;
  public DataInputStream in = null;
  public DataOutputStream out = null;
  public int port = 0;
  public String type = "";

  public Server(int port, String type) {
    this.port = port;
    this.type = type;
  }

  public void run() {
    try {
      this.serverSocket = new ServerSocket(this.port);
      this.socket = this.serverSocket.accept();
      this.in = new DataInputStream(socket.getInputStream());
      this.out = new DataOutputStream(socket.getOutputStream());

      String messageFromMidServer = this.in.readUTF();
      while(messageFromMidServer.length() > 0) { 
        System.out.println(messageFromMidServer + " " + this.type);
        out.writeUTF("200");
        out.flush();
        messageFromMidServer = in.readUTF();
      }

      this.socket.close();
      this.serverSocket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
public class GroupServer {
  public static void main(String[] args) {
    int goldPort = Integer.parseInt(args[0]);
    int silverPort = Integer.parseInt(args[1]);
    int platinumPort = Integer.parseInt(args[2]);

    Runnable goldServer = new Server(goldPort, "gold");
    Runnable silverServer = new Server(silverPort, "silver");
    Runnable platinumServer = new Server(platinumPort, "platinum");

    ExecutorService executorService = Executors.newFixedThreadPool(3);
    executorService.execute(goldServer);
    executorService.execute(silverServer);
    executorService.execute(platinumServer);

    executorService.shutdown();
  }
}
