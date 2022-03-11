
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
  public DataOutputStream groupServerOut = null;
  public String type = "";
  public String[] items = new String[7];
  public int port = 0;
  public int points = 0;

  public Server(int port, String type) {
    this.port = port;
    this.type = type;
    this.fetchData();
  }

  public void fetchData() {
    int i = 0;
    try {
      File dataFile = new File("data/gold.txt");
      Scanner myReader = new Scanner(dataFile);
      while(myReader.hasNextLine()) {
        String data = myReader.nextLine();
        this.items[i] = data;
        i++;
      }
      myReader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void run() {
    try {
      this.serverSocket = new ServerSocket(this.port);
      this.socket = this.serverSocket.accept();
      this.groupServerIn = new DataInputStream(socket.getInputStream());
      this.groupServerOut = new DataOutputStream(socket.getOutputStream());

      this.groupServerOut.writeUTF("Connected to Group server");
      this.groupServerOut.flush();

      String messageFromClient = this.groupServerIn.readUTF();

      while (messageFromClient.length() > 0) {
        System.out.println("Client: " + messageFromClient);
        if (messageFromClient.contains("user-credentials")) {
          String[] credentials = messageFromClient.split("_");
          this.points = Integer.parseInt(credentials[1]);
        }
        else if (messageFromClient.equals("ls")) {
          String itemstr = "items-";
          for (int i = 0; i < this.items.length; i++) {
            itemstr = itemstr + this.items[i] + "#nn#";
          }
          itemstr = itemstr + "Remaining points on your account are " + this.points;
          this.groupServerOut.writeUTF(itemstr);
          this.groupServerOut.flush();
        }
        else if (
          messageFromClient.equals("1") ||
          messageFromClient.equals("2") ||
          messageFromClient.equals("3") ||
          messageFromClient.equals("4") ||
          messageFromClient.equals("5") ||
          messageFromClient.equals("6") ||
          messageFromClient.equals("7")
        ) {
          int index = Integer.parseInt(messageFromClient) - 1;
          String[] item = this.items[index].split(" ");
          if (this.points >= Integer.parseInt(item[item.length - 1])) {
            this.points = this.points - Integer.parseInt(item[item.length - 1]);
            this.groupServerOut.writeUTF("purchased-item_" + this.items[index] + "_" + this.points);
          } else {
            this.groupServerOut.writeUTF("cannot-purchase_" + this.points);
          }
          this.groupServerOut.flush();
        }
        else if (messageFromClient.equals("close")) {
          this.groupServerIn.close();
          this.groupServerOut.close();
          this.socket.close();
          this.serverSocket.close();
        }
        else {
          this.groupServerOut.writeUTF("200");
          this.groupServerOut.flush();
        }
        messageFromClient = this.groupServerIn.readUTF();
      }
      this.groupServerIn.close();
      this.groupServerOut.close();
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
