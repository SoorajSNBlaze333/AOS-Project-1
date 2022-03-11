
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

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

  public void logout() {
    this.username = "";
    this.password = "";
    this.groupServerType = "";
    this.userPoints = 0;
    this.hasLoggedIn = false;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}

public class SivadasanNairP1MidServer {
  public static void print(String message) { System.out.println(message); }

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

    Auth user = new Auth("auth/userList.txt");
    try {
      String ipAddress = args[0];
      int creds = 0;
      
      ServerSocket serverSocket = new ServerSocket(10432);
      Socket socket = serverSocket.accept();

      DataInputStream in = new DataInputStream(socket.getInputStream());
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());

      out.writeUTF("connected-successfully");
      out.flush();

      String messageFromClient = in.readUTF();
      
      while(messageFromClient.length() > 0) {
        print("Client: " + messageFromClient);
        switch (messageFromClient) {
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
          case "ls": {
            out.writeUTF("access-forbidden");
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