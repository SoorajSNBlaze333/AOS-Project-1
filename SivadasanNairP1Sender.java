
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class SivadasanNairP1Sender {
  public static void print(String message) {
    System.out.println(message);
  }

  public static void main(String[] args) {
    if (args.length < 1) print("Please provide the IP Address and Port Number");

    try {
      InetAddress ipAddress = InetAddress.getByName(args[0]);
      int port = Integer.parseInt(args[1]);
      
      Socket socket = new Socket(ipAddress, port);

      DataInputStream in = new DataInputStream(socket.getInputStream());
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

      String messageFromMidServer = in.readUTF();
      String message = "";
      
      while(!messageFromMidServer.equals("connection-closed")) {
        print(messageFromMidServer); // -> debug statement
        if (messageFromMidServer.equals("100")) {
          print("Connection established with server!");
        }
        if (messageFromMidServer.equals("get-credentials")) {
          print("Enter the username and password");
        }
        if (messageFromMidServer.equals("401")) {
          print("Please login to the server!!");
        }
        if (messageFromMidServer.equals("logged-in")) {
          print("You are already logged in to the server!!");
        }          
        message = br.readLine();
        out.writeUTF(message);
        
        messageFromMidServer = in.readUTF();
      }

      in.close();
      out.close();
      socket.close();
    } catch (Exception e) {
      e.printStackTrace(); 
    }
  }
}