import java.io.DataInputStream;
import java.io.DataOutputStream;
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
  public String serverGroup = "";
  
  public Auth(String filename) {
    this.filename = filename;
  }

  public void login() {
    try {
      File authFile = new File(this.filename);
      Scanner myReader = new Scanner(authFile);  
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        System.out.println(data);
      }
      myReader.close();
    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
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

public class MidServer {
  public static void main(String[] args) {
    Auth user = new Auth("userList.txt");
    try {
      int port = Integer.parseInt(args[0]);
      ServerSocket serverSocket = new ServerSocket(port);
      Socket socket = serverSocket.accept();

      DataInputStream in = new DataInputStream(socket.getInputStream());
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());
      // BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

      out.writeUTF("100");
      out.flush();

      String messageFromClient = in.readUTF();
      int username = 0;
      int password = 0;
      // String message = "";
      while(messageFromClient.length() > 0) {
        System.out.println(messageFromClient); // -> debug statement
        // message = br.readLine();
        if (messageFromClient.equals("login")) {
          if (user.role.length() > 0) out.writeUTF("logged-in");
          else {
            if (username == 0) {
              username = 1;
              out.writeUTF("get-username");
            }
          }
          out.flush();
        }
        if (messageFromClient.equals("items")) {
          if (user.role.length() > 0) { /*code here to connect to group server*/ }
          else {
            out.writeUTF("401");
            out.flush();
          }
        } else {
          if (username == 1) {
            user.setUsername(messageFromClient);
            username = 0;
            password = 1;
            out.writeUTF("get-password");
            out.flush();
          } else if (username == 0 && password == 1) {
            user.setPassword(messageFromClient);
            password = 0;
            out.writeUTF("200");
            out.flush();
          } else {
            out.writeUTF("200");
            out.flush();
          }
        }
        messageFromClient = in.readUTF();
        // if (user.hasLoggedIn == false && clientMessage.contains("username-")) {
        //   user.setUsername(clientMessage);
        // }
        // if (user.hasLoggedIn == false && clientMessage.contains("password-")) {
        //   user.setPassword(clientMessage);
        // }

      }

      in.close();
      out.close();
      serverSocket.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
