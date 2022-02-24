// import java.io.BufferedReader;
// import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MidServer {
  public static void main(String[] args) {
    try {
      int port = Integer.parseInt(args[0]);
      ServerSocket serverSocket = new ServerSocket(port);
      Socket socket = serverSocket.accept();

      // BufferedReader in = new BufferedReader( new InputStreamReader(socket.getInputStream()));
      PrintWriter out = new PrintWriter(socket.getOutputStream());

      out.write("Message from server");
      out.flush();

      out.close();
      serverSocket.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
