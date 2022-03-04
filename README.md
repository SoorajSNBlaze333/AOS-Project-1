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
 as, 
 ```shell
 java SivadasanNairP1Sender 127.0.0.1 10432
 ```
 - We will recieve the arguments passed from the command line using 
 ```java
  public static void main(String[] args) {
    if (args.length < 1) print("Please provide the IP Address and Port Number");

    try {
      InetAddress ipAddress = InetAddress.getByName(args[0]);
      int port = Integer.parseInt(args[1]);
      ...
 ```
 - We will now create a socket object to connect to the server. Along with the socket, we also need to have a input stream and output stream to recieve and send messages from and to the server socket. We will be using the `Socket, DataInputStream, DataOutputStream and BufferedReader` Classes such as,
 ```java
  Socket socket = new Socket(ipAddress, port);
  DataInputStream serverIn = new DataInputStream(socket.getInputStream());
  DataOutputStream clientOut = new DataOutputStream(socket.getOutputStream());
  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 ```
 - Next we need to create a variable that will store the input that is coming from the server such as,
 ```java
  String messageFromMidServer = serverIn.readUTF();
 ```
 - Next let's keep listening for any message that comes to the sender using a while loop. We will listen for a `connection-closed` to terminate the connection. The code inside the while loop will always be executed as long as the server keeps responding to the client.
 ```java
  while(!messageFromMidServer.equals("connection-closed")) { ...
 ```
 - Let's have simple response handler functions, basically print statements which will let us know what kind of a message is coming from server. This was inspired from a web client-server request-response architecture.
 ```java
  if (messageFromMidServer.equals("100")) {
    print("Connection established with server!");
  }
  if (messageFromMidServer.equals("get-credentials")) {
    print("Enter the username and password");
  }
  if (messageFromMidServer.equals("401")) {
    print("Please login to the server!!");
  }
  if (messageFromMidServer.equals("logged-in")) {
    print("You are already logged in to the server!!");
  }          
  messageFromClient = br.readLine();
  out.writeUTF(messageFromClient);
  
  messageFromMidServer = serverIn.readUTF();
 ```
 - Here, let's create a generic function called `print(String message)` to display any string message. This was a good approach when there was a need for using `System.out.println` multiple times in the same file.
 ```java
  public static void print(String message) {
    System.out.println(message);
  }
 ```
 - We need to close all the objects related to socket, which includes the socket itself and the input and output streams.
 ```java
  serverIn.close();
  clientOut.close();
  socket.close();
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