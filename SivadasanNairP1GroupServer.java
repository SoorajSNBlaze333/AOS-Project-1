
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Server implements Runnable {
  public ServerSocket serverSocket = null;
  public Socket socket = null;
  public DataInputStream groupServerIn = null;
  public DataOutputStream midServerOut = null;
  public String type = "";
  public int port = 0;

  public Server(int port, String type) {
    this.port = port;
    this.type = type;
  }

  public void run() {
    try {
      this.serverSocket = new ServerSocket(this.port);
      this.socket = this.serverSocket.accept();
      this.groupServerIn = new DataInputStream(socket.getInputStream());
      this.midServerOut = new DataOutputStream(socket.getOutputStream());

      String messageFromMidServer = this.groupServerIn.readUTF();
      while(messageFromMidServer.length() > 0) {
        System.out.println(messageFromMidServer + " " + this.type);
        switch (messageFromMidServer) {
          case "items": {
            File dataFile = new File("data/" + this.type + ".txt");
            Scanner myReader = new Scanner(dataFile);
            while (myReader.hasNextLine()) {
              String data = myReader.nextLine();
              midServerOut.writeUTF("items-" + data);
            }
            midServerOut.flush();
            myReader.close();
            break;
          }
          default: {
            midServerOut.writeUTF("200");
            midServerOut.flush();
            break;
          }
        }
        messageFromMidServer = groupServerIn.readUTF();
      }
      this.groupServerIn.close();
      this.midServerOut.close();
      this.socket.close();
      this.serverSocket.close();
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
}
public class SivadasanNairP1GroupServer {
  public static void main(String[] args) {
    Runnable silverServerThread = new Server(10433, "silver");
    Runnable goldServerThread = new Server(10434, "gold");
    Runnable platinumServerThread = new Server(10435, "platinum");

    ExecutorService executorService = Executors.newFixedThreadPool(3);
    executorService.execute(goldServerThread);
    executorService.execute(silverServerThread);
    executorService.execute(platinumServerThread);

    executorService.shutdown();
  }
}
