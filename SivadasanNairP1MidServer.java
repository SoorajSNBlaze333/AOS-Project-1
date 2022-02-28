
import java.net.InetAddress;
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
  public String userPoints = "";
  public Boolean hasLoggedIn = false;
  
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
          this.userPoints = credentials[2];
        } 
      }
      if (this.userPoints.length() > 0) this.hasLoggedIn = true;
      else this.hasLoggedIn = false;
      scanner.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } 
  }

  public void logout() {
    this.username = "";
    this.password = "";
    this.userPoints = "";
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
  public static void main(String[] args) {
    if (args.length < 1) print("Please provide the IP Address and Port Number");

    Auth user = new Auth("auth/userList.txt");
    try {
      InetAddress ipAddress = InetAddress.getByName(args[0]);
      int port = Integer.parseInt(args[1]);
      int creds = 0;
      
      ServerSocket serverSocket = new ServerSocket(port);
      Socket socket = serverSocket.accept();

      DataInputStream in = new DataInputStream(socket.getInputStream());
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());

      Socket goldServerSocket = new Socket(ipAddress, 10433);
      Socket silverServerSocket = new Socket(ipAddress, 10434);
      Socket platinumServerSocket = new Socket(ipAddress, 10435);

      out.writeUTF("100");
      out.flush();

      String messageFromClient = in.readUTF();
      
      while(messageFromClient.length() > 0) {
        print(messageFromClient); // -> debug statement
        if (user.hasLoggedIn) {
          switch (messageFromClient) {
            case "login": {
              out.writeUTF("logged-in");
              out.flush();
              break;
            }
            case "logout": {
              user.logout();
              out.writeUTF("logged-out");
              out.flush();
              break;
            }
            default: {
              Socket groupSocket = null;
              if (user.userPoints.equals("gold")) groupSocket = goldServerSocket;
              else if (user.userPoints.equals("silver")) groupSocket = silverServerSocket;
              else if (user.userPoints.equals("platinum")) groupSocket = platinumServerSocket;
              DataInputStream din = new DataInputStream(groupSocket.getInputStream());
              DataOutputStream dout = new DataOutputStream(groupSocket.getOutputStream());
              try {
                dout.writeUTF(messageFromClient);
                dout.flush();
                String messageFromGroupServer = din.readUTF();
                if (messageFromGroupServer.length() > 0) {
                  out.writeUTF(messageFromGroupServer);
                  out.flush();
                }
              } catch (Exception e) { e.printStackTrace(); }
              break;
            }
          }
        } else {
          switch (messageFromClient) {
            case "login": {
              if (user.userPoints.length() > 0) out.writeUTF("logged-in");
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
              out.writeUTF("200");
              out.flush();
              break;
            }
            case "items": {
              out.writeUTF("401");
              out.flush();
              break;
            }
            default: {
              if (creds == 1) {
                String[] credentials = messageFromClient.split(" ");
                user.setUsername(credentials[0]);
                user.setPassword(credentials[1]);
                creds = 0;
                out.writeUTF("200");
                out.flush();
              } else {
                out.writeUTF("200");
                out.flush();
              }
              break;
            }
          }
        }
        messageFromClient = in.readUTF();
      }

      in.close();
      out.close();
      socket.close();
      goldServerSocket.close();
      silverServerSocket.close();
      platinumServerSocket.close();
      serverSocket.close();
    } catch(Exception e) { e.printStackTrace(); }
  }
}
