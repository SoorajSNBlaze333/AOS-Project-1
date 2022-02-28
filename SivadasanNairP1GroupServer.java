
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Server implements Runnable {
  public ServerSocket serverSocket = null;
  public Socket socket = null;
  public DataInputStream in = null;
  public DataOutputStream out = null;
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
      this.in = new DataInputStream(socket.getInputStream());
      this.out = new DataOutputStream(socket.getOutputStream());

      String messageFromMidServer = this.in.readUTF();
      while(messageFromMidServer.length() > 0) {
        System.out.println(messageFromMidServer + " " + this.type);
        switch (messageFromMidServer) {
          case "items": {
            try {
              File dataFile = new File("data/" + this.type + ".txt");
              Scanner myReader = new Scanner(dataFile);
              while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                out.writeUTF("items-" + data);
              }
              out.flush();
              myReader.close();
            } catch (FileNotFoundException e) {
              System.out.println("An error occurred.");
              e.printStackTrace();
            } 
            break;
          }
          default: {
            out.writeUTF("200");
            out.flush();
            break;
          }
        }
        messageFromMidServer = in.readUTF();
      }
      this.socket.close();
      this.serverSocket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
public class SivadasanNairP1GroupServer {
  public static void main(String[] args) {
    int goldPort = 10433;
    int silverPort = 10434;
    int platinumPort = 10435;

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
