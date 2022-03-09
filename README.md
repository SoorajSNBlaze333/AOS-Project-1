# Project 01 - Distributed Network
> Create a simple distributed network. There is a sender that communicates with the mid-server. The mid-server forwards the messages from the client to the dedicated the Group server.

## 1.Project Analysis
Here we need first split the problem into 2 sections (Server and Client). We can visualize that the Sender and Mid-Server is going to follow a server-client architecture and the Mid-Server and Group-server is again going to follow another server-client architecture. We will be using `Java` language to create this distributed system.

## 2. Flowchart
![Alt text](assets/flowchart.png)

## 3. Basic Setup
We will start with the Client/Sender application.

### 3.a. Sender Program
 - Let's create a `SivadasanNairP1Sender.java` file.
 - We will get the IP address and the Port number of the MidServer from the command line arguments such
 as `java SivadasanNairP1Sender 127.0.0.1 10432`
 - We will recieve the arguments passed from the command line to use as the IP Address and Port number. 
 - We will now create a socket object to connect to the server. Along with the socket, we also need to have a input stream and output stream to recieve and send messages from and to the server socket. We will be using the `Socket, DataInputStream, DataOutputStream and BufferedReader`.
 - Next we need to create a variable that will store the input that is coming from the server.
 - Next let's keep listening for any message that comes to the sender using a while loop. We will listen for a `connection-closed` to terminate the connection. The code inside the while loop will always be executed as long as the server keeps responding to the client.
 - Let's have simple response handler functions, basically print statements which will let us know what kind of a message is coming from server. This was inspired from a web client-server request-response architecture.
 - Here, let's create a generic function called `print(String message)` to display any string message. This was a good approach when there was a need for using `System.out.println` multiple times in the same file.
 ```java
  public static void print(String message) {
    System.out.println(message);
  }
 ```
 - We need to close all the objects related to socket, which includes the socket itself and the input and output streams.
 - `SivadasanNairP1Sender.java`
 ```java
  import java.io.BufferedReader;
  import java.io.DataInputStream;
  import java.io.DataOutputStream;
  import java.io.InputStreamReader;
  import java.net.InetAddress;
  import java.net.Socket;

  public class SivadasanNairP1Sender {
    public static Boolean loggedIn = false;

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
          if (messageFromMidServer.equals("connected-successfully")) {
            print("Server: Connection established with server. Enter 'login' to login to the server.");
          }
          else if (messageFromMidServer.equals("get-credentials")) {
            print("Server: Enter the username and password.");
          }
          else if (messageFromMidServer.equals("got-credentials")) {
            print("Server: Username and password recieved at server, enter 'submit' to continue to server.");
          }
          else if (messageFromMidServer.equals("access-forbidden")) {
            print("Server: Please login to the server before accessing the shopping list!!");
          }
          else if (messageFromMidServer.equals("login-success")) {
            print("Server: You have successfully logged into the server.");
          }
          else if (messageFromMidServer.equals("logged-in")) {
            print("Server: You are already logged in to the server!!");
          }   
          else if (messageFromMidServer.equals("logged-out")) {
            print("Server: You have successfully logged out from the server.");
          }    
          else if (messageFromMidServer.contains("items-"))  {
            String[] items = messageFromMidServer.split("-");
            String[] cleanedData = items[1].split("#nn#");
            print("Server: Here are the list of items you can choose from!");
            for (int i = 0; i < cleanedData.length; i++) {
              print(cleanedData[i]);
            }
          } 
          else {
            print("Server: Please enter a valid command!");
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
 ```

### 3.b. MidServer Program
 - Let's create a `SivadasanNairP1MidServer.java` file.
 - We will get the IP address of the Group Server and the Port number of the Sender from the command line arguments such
 as, 
 ```shell
 java SivadasanNairP1MidServer 127.0.0.1 10432
 ```
 - MidServer is almost the same as the Sender program, the only difference being that MidServer is responsible for
    - Responding all the messages and responses from Sender and GroupServer.
    - Handling the authentication from the Sender.
 - The MidServer keeps track of the user's authentication status using an `Auth` class. The `Auth` class is reponsible for login and logout of the user.
 - The `Auth` class keeps track of the following
 ```java
  public String filename = "";
  public String username = "";
  public String password = "";
  public String userPoints = "";
  public Boolean hasLoggedIn = false;
 ```
 - Then we write a simple login function, that uses the username and password and compares it to the username and password in the file `auth/userList.txt`.
 ```java
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
  ```
  - Let's write a logout function too, just in case we need to test other users.
  ```java
  public void logout() {
    this.username = "";
    this.password = "";
    this.userPoints = "";
    this.hasLoggedIn = false;
  }
  ```
  - Now we will connect the MidServer to the group servers. In our case we will be using 3 types of Group servers (silver, gold and platinum). We will only open the socket communication to the server only once the user is an authenticated user.
  ```java
    Socket silverServerSocket = new Socket(ipAddress, 10433);
    Socket goldServerSocket = new Socket(ipAddress, 10434);
    Socket platinumServerSocket = new Socket(ipAddress, 10435);

    DataInputStream groupServerIn = null;
    DataOutputStream groupServerOut = null;
  ```
  - We will now write set of conditions that will manage the data between the sender and the mid-server and the mid-server and the group-server.
  ```java
  while(messageFromClient.length() > 0) {
    print("Client: " + messageFromClient);
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
        case "items": {
          Socket groupSocket = null;
          if (user.userPoints.equals("gold")) groupSocket = goldServerSocket;
          else if (user.userPoints.equals("silver")) groupSocket = silverServerSocket;
          else if (user.userPoints.equals("platinum")) groupSocket = platinumServerSocket;
          groupServerIn = new DataInputStream(groupSocket.getInputStream());
          groupServerOut = new DataOutputStream(groupSocket.getOutputStream());
          groupServerOut.writeUTF(messageFromClient);
          groupServerOut.flush();
          String messageFromGroupServer = groupServerIn.readUTF();
          if (messageFromGroupServer.length() > 0) {
            print("Group Server: " + messageFromClient);
            out.writeUTF(messageFromGroupServer);
            out.flush();
          }
        }
        default: {
          out.writeUTF("200");
          out.flush();
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
          if (user.hasLoggedIn) out.writeUTF("login-success");
          out.flush();
          break;
        }
        case "items": {
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
    }
    messageFromClient = in.readUTF();
  }
  ``` 
  - We will close all our socket objects to prevent any memory leaks
  ```java
    in.close();
    out.close();
    socket.close();
    goldServerSocket.close();
    silverServerSocket.close();
    platinumServerSocket.close();
    serverSocket.close();
  ```

### 3.c. MidServer Program
-  Let's create a `SivadasanNairP1GroupServer.java` file.