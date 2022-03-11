/* 
  MidServer Program
  AOS Project-1
  Sooraj Sivadasan Nair, 2000432
  02/24/2022
*/

import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

// Auth class will be responsible for setting the user credentials and validating the user login
class Auth {
  public String filename = "";
  public String username = "";
  public String password = "";
  public String groupServerType = "";
  public Boolean hasLoggedIn = false;
  public int userPoints = 0;
  
  public Auth(String filename) {
    this.filename = filename;
  }

  // This method is responsible for reading the contents from userList.txt and validating the user 
  // to get the type of the group server the user belongs to and the number of points the user has
  public void login() {
    try {
      File authFile = new File(this.filename);
      Scanner scanner = new Scanner(authFile);  
      while (scanner.hasNextLine()) {
        String fileData = scanner.nextLine();
        String[] credentials = fileData.split(" ");
        if (
          credentials[0].equals(this.username) && 
          credentials[1].equals(this.password)) {
          this.groupServerType = credentials[2];
          this.userPoints = Integer.parseInt(credentials[3]);
          this.hasLoggedIn = true;
          break;
        }
      }
      scanner.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } 
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}

public class SivadasanNairP1MidServer {
  // We will have a smaller generic function instead of writing System.out.println everytime
  public static void print(String message) { System.out.println(message); }

  // This is just a debug/delay function to make the lines of codes execute below the function after a delay
  public static void wait(int ms) {
    try {
      Thread.sleep(ms);
    }
    catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }
  
  public static void main(String[] args) {
    if (args.length < 1) print("Please provide the IP Address and Port Number");

    // The Auth object will be contain the user credentials and the login activity
    Auth user = new Auth("auth/userList.txt");
    try {
      String ipAddress = args[0];
      int creds = 0;
      
      ServerSocket serverSocket = new ServerSocket(10432);
      Socket socket = serverSocket.accept();
      DataInputStream in = new DataInputStream(socket.getInputStream());
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());

      System.out.println("Waiting for message from Sender");

      out.writeUTF("connected-successfully");
      out.flush();

      System.out.println("Sender connected successfully!");

      String messageFromClient = in.readUTF();
      
      while(messageFromClient.length() > 0) {
        print("Client: " + messageFromClient);
        switch (messageFromClient) {
          // This step is used to tell the server that the next value that the Sender is going to send is
          // the user credentials
          case "login": {
            if (user.groupServerType.length() > 0) out.writeUTF("logged-in");
            else {
              if (creds == 0) {
                creds = 1;
                out.writeUTF("get-credentials");
              }
            }
            out.flush();
            break;
          }
          // This step is used to tell the server to validate the credentials that are receieved in the
          // previous step
          case "submit": {
            user.login();
            if (user.hasLoggedIn) {
              int groupServerPort = 0;
              if (user.groupServerType.equals("gold")) groupServerPort = 10433;
              else if (user.groupServerType.equals("silver")) groupServerPort = 10434;
              else if (user.groupServerType.equals("platinum")) groupServerPort = 10435;
              wait(1000);
              out.writeUTF("login-success_" + ipAddress + "_" + groupServerPort + "_" + user.groupServerType + "_" + user.userPoints);
            }
            out.flush();
            break;
          }
          default: {
            if (creds == 1) {
              String[] credentials = messageFromClient.split(" ");
              user.setUsername(credentials[0]);
              user.setPassword(credentials[1]);
              creds = 0;
              out.writeUTF("got-credentials");
              out.flush();
            } else {
              out.writeUTF("200");
              out.flush();
            }
            break;
          }
        }
        messageFromClient = in.readUTF();
      }

      in.close();
      out.close();
      socket.close();
      serverSocket.close();
    } catch(Exception e) { e.printStackTrace(); }
  }
}