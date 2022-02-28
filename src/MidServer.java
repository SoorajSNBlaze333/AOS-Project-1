package src;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class Auth {
  public String filename = "";
  public String username = "";
  public String password = "";
  public String role = "";
  public Boolean hasLoggedIn = false;
  
  public Auth(String filename) {
    this.filename = filename;
  }

  public void login() {
    try {
      File authFile = new File(this.filename);
      Scanner myReader = new Scanner(authFile);  
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        String[] credentials = data.split(" ");
        System.out.println("Password is " + this.password);
        System.out.println(this.username + " " + this.password + " " + credentials[0] + " " + credentials[1]);
        if (
          credentials[0].equals(this.username) && 
          credentials[1].equals(this.password)
        ) {
          this.role = credentials[2];
        } 
      }
      if (this.role.length() > 0) {
        System.out.println("Valid user " + this.role);
        this.hasLoggedIn = true;
      } else {
        System.out.println("Not a valid user");
        this.hasLoggedIn = false;
      }
      myReader.close();
    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    } 
  }

  public void logout() {
    this.username = "";
    this.password = "";
    this.role = "";
    this.hasLoggedIn = false;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}

class GroupServer implements Runnable {
  public InetAddress ipaddress = null;
  public int port = 0;
  public Socket socket = null;
  public DataInputStream din = null;
  public DataOutputStream dout = null;

  public GroupServer(InetAddress ipAddress, int port) {
    this.ipaddress = ipAddress;
    this.port = port;
  }

  public void run() {
    try {
      this.socket = new Socket(this.ipaddress, this.port);
      this.din = new DataInputStream(this.socket.getInputStream());
      this.dout = new DataOutputStream(this.socket.getOutputStream());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

public class MidServer {
  public static void main(String[] args) {
    Auth user = new Auth("./auth/userList");
    try {
      InetAddress ipAddress = InetAddress.getByName(args[0]);
      int port = Integer.parseInt(args[1]);
      
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
      int creds = 0;
      while(messageFromClient.length() > 0) {
        System.out.println(messageFromClient); // -> debug statement
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
              if (user.role.equals("gold")) groupSocket = goldServerSocket;
              else if (user.role.equals("silver")) groupSocket = silverServerSocket;
              else if (user.role.equals("platinum")) groupSocket = platinumServerSocket;
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
              } catch (Exception e) {
                e.printStackTrace();
              }
              break;
            }
          }
        } else {
          switch (messageFromClient) {
            case "login": {
              if (user.role.length() > 0) out.writeUTF("logged-in");
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
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
