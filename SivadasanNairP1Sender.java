
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
    if (args.length < 2) print("Please provide the IP Address and Port Number");

    try {
      InetAddress ipAddress = InetAddress.getByName(args[0]);
      int port = Integer.parseInt(args[1]);
      
      Socket socket = new Socket(ipAddress, port);
      DataInputStream serverIn = new DataInputStream(socket.getInputStream());
      DataOutputStream clientOut = new DataOutputStream(socket.getOutputStream());
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

      String messageFromMidServer = serverIn.readUTF();
      String messageFromClient = "";
      
      while(!messageFromMidServer.equals("connection-closed")) {
        // print(messageFromMidServer);
        if (messageFromMidServer.equals("connected-successfully")) {
          print("Server: Connection established with server. Enter 'login' to login to the server.");
        }
        if (messageFromMidServer.equals("get-credentials")) {
          print("Server: Enter the username and password.");
        }
        if (messageFromMidServer.equals("got-credentials")) {
          print("Server: Username and password recieved at server, enter 'submit' to continue to server.");
        }
        if (messageFromMidServer.equals("access-forbidden")) {
          print("Server: Please login to the server before accessing the shopping list!!");
        }
        if (messageFromMidServer.equals("login-success")) {
          print("Server: You have successfully logged into the server.");
        }
        if (messageFromMidServer.equals("logged-in")) {
          print("Server: You are already logged in to the server!!");
        }   
        if (messageFromMidServer.equals("logged-out")) {
          print("Server: You have successfully logged out from the server.");
        }    
        if (messageFromMidServer.contains("items-"))  {
          String[] items = messageFromMidServer.split("-");
          print("Server: " + items[1]);
        }  
        messageFromClient = br.readLine();
        clientOut.writeUTF(messageFromClient);
        
        messageFromMidServer = serverIn.readUTF();
      }

      clientOut.close();
      serverIn.close();
      socket.close();
    } catch (Exception e) {
      e.printStackTrace(); 
    }
  }
}