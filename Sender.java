import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public class Sender {
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Please provide the IP Address and Port Number");
    }
    try {
      InetAddress ipAddress = InetAddress.getByName(args[0]);
      int port = Integer.parseInt(args[1]);
      
      try {
        Socket socket = new Socket(ipAddress, port);

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String messageFromServer = in.readUTF();
        String message = "";
        
        while(!messageFromServer.equals("connection-closed")) {
          System.out.println(messageFromServer); // -> debug statement
          if (messageFromServer.equals("100")) {
            System.out.println("Connection established with server!");
          }
          if (messageFromServer.equals("get-username")) {
            System.out.println("Enter the username");
          }
          if (messageFromServer.equals("get-password")) {
            System.out.println("Enter the password");
          }
          if (messageFromServer.equals("401")) {
            System.out.println("Please login to the server!!");
          }
          if (messageFromServer.equals("logged-in")) {
            System.out.println("You are already logged in to the server!!");
          }
          message = br.readLine();
          out.writeUTF(message);

          // if (serverMessage.equals("500")) {
          //   System.out.println("Server error. Please try again later!!");
          // }
          // String username = getUsername(scanner);
          // String password = getPassword(scanner);
          // out.println("username-" + username);
          // out.flush();
          // out.println("password-" + password);
          // out.flush();

          // String input = getUserInput(scanner);
          // while(!input.equals("exit")) {
          //   out.println(input);
          //   input = getUserInput(scanner);
          // }
          // break; --> debugging line
          messageFromServer = in.readUTF();
        }

        in.close();
        out.close();
        socket.close();
      } catch (Exception e) { // error handling for socket programming
        e.printStackTrace(); 
      }
    } catch (Exception e) { // error handling for inet address conversion
      e.printStackTrace(); 
    }
  }
}