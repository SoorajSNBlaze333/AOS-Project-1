import java.io.BufferedReader;
import java.io.InputStreamReader;
// import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public class Sender {
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Please provide the IP Address and Port Number");
    }
    // String username = "";
    // String password = "";
    try {
      InetAddress ipAddress = InetAddress.getByName(args[0]);
      int port = Integer.parseInt(args[1]);
      
      try {
        Socket socket = new Socket(ipAddress, port);

        // PrintStream out = new PrintStream(socket.getOutputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String line = in.readLine();
        while( line != null ) {
            System.out.println( line );
            line = in.readLine();
        }

        in.close();
        socket.close();
      } catch (Exception e) { // error handling for socket programming
        e.printStackTrace(); 
      }
    } catch (Exception e) { // error handling for inet address conversion
      e.printStackTrace(); 
    }
  }
}