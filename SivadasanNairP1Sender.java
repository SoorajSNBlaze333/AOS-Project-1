/* 
  Sender Program
  AOS Project-1
  Sooraj Sivadasan Nair, 2000432
  02/24/2022
*/


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class SivadasanNairP1Sender {
  private static InetAddress ipAddress = null;
  private static Socket socket = null;
  private static DataInputStream serverIn = null;
  private static DataOutputStream clientOut = null;
  private static BufferedReader br = null;
  private static String messageFromServer = "";
  private static String messageFromClient = "";
  private static String groupServerType = "";
  private static Boolean disconnectFromMidServer = false;
  private static int port = 0;
  private static int userPoints = 0;

  // We will have a smaller generic function instead of writing System.out.println everytime
  public static void print(String message) {
    System.out.println(message);
  }

  public static void main(String[] args) {
    if (args.length < 2) print("Please provide the IP Address and Port Number");

    try {
      ipAddress = InetAddress.getByName(args[0]);      
      socket = new Socket(ipAddress, 10432);
      serverIn = new DataInputStream(socket.getInputStream());
      clientOut = new DataOutputStream(socket.getOutputStream());
      br = new BufferedReader(new InputStreamReader(System.in));

      messageFromServer = serverIn.readUTF();
      messageFromClient = "";
      
      // Handle all messages from the Mid Server
      while(!messageFromServer.equals("connection-closed") && !disconnectFromMidServer) {
        if (messageFromServer.equals("connected-successfully")) {
          print("Server: Connection established with server. Enter 'login' to login to the server.");
        }
        else if (messageFromServer.equals("get-credentials")) {
          print("Server: Enter the username and password.");
        }
        else if (messageFromServer.equals("got-credentials")) {
          print("Server: Username and password recieved at server, enter 'submit' to continue to server.");
        }
        else if (messageFromServer.contains("login-success")) {
          print("Server: You have successfully logged into the server.");
          
          String[] serverDetails = messageFromServer.split("_");
          ipAddress = InetAddress.getByName(serverDetails[1]);
          port = Integer.parseInt(serverDetails[2]);
          groupServerType = serverDetails[3];
          userPoints = Integer.parseInt(serverDetails[4]);

          disconnectFromMidServer = true;

          clientOut.close();
          serverIn.close();
          socket.close();
          break;
        } 
        else {
          print("Server: Please enter a valid command!");
        } 
        messageFromClient = br.readLine();
        clientOut.writeUTF(messageFromClient);
        messageFromServer = serverIn.readUTF();
      }

      // Here we will connect to the respective Group server
      socket = new Socket(ipAddress, port);
      serverIn = new DataInputStream(socket.getInputStream());
      clientOut = new DataOutputStream(socket.getOutputStream());
      br = new BufferedReader(new InputStreamReader(System.in));

      messageFromServer = serverIn.readUTF();
      messageFromClient = "";

      clientOut.writeUTF("user-credentials_" + userPoints);
      clientOut.flush();

      // Handle all messages from Group Server
      while(messageFromServer.length() > 0) {
        if (messageFromServer.contains("items-")) {
          String[] items = messageFromServer.split("-");
          String[] cleanedData = items[1].split("#nn#");
          print("[" + groupServerType + " server]: Here are the list of items you can choose from!");
          for (int i = 0; i < cleanedData.length; i++) {
            print(cleanedData[i]);
          }
        } 
        else if (messageFromServer.contains("purchased-item")) {
          String[] cleanedData = messageFromServer.split("_");
          print("[" + groupServerType + " server]: Great you have purchased " + cleanedData[1] + ". You have " + cleanedData[2] + " points remaining on your account.");
        }
        else if (messageFromServer.contains("cannot-purchase")) {
          print("[" + groupServerType + " server]: Sorry, but you cannot purchase this item as your points are not sufficient.");
        }
        else {
          System.out.println("[" + groupServerType + " server]: " + messageFromServer);
        }
        messageFromClient = br.readLine();
        clientOut.writeUTF(messageFromClient);
        messageFromServer = serverIn.readUTF();
      }
    } catch (Exception e) {
      e.printStackTrace(); 
    }
  }
}