// CMN Summative Assessment 2021
// --------Client-----------
// Communication using Go-Back-N protocol
// @version 10/10/2021

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.ByteBuffer;

class Client {
  private final int BUFFER = 1024;
  private final String FILEPATH = "../umbrella.txt";
  private final int WINDOW_SIZE = 5;
  private InetAddress IPAddress;
  private int port;
  private byte[] sendData = new byte[BUFFER];
  private byte[] receiveData = new byte[BUFFER];
  private DatagramSocket clientSocket;
  private DatagramPacket receivePacket;
  private int sequenceNo;
  private int nextSequenceNo = 0;

  public void goBackN() throws Exception {
    // Creating a socket
    clientSocket = new DatagramSocket();
    clientSocket.setSoTimeout( 1000 );
    
    getUserInput();
    int dynamicWindowSize = WINDOW_SIZE;

    // Used 10 frames to demo
    int frame = 0;
    while(frame != 11) {
      for (sequenceNo = nextSequenceNo; sequenceNo < dynamicWindowSize; sequenceNo++) {
        buildPayload();
        try{
          sendPacket();
        } catch( SocketTimeoutException exception ){
          System.out.println("There was a socket timeout.");
        }
      }
      if(receivePacket()){
        setSequenceNo();
        System.out.println("Received ack with sequence number: " + sequenceNo);
        nextSequenceNo = dynamicWindowSize;
        dynamicWindowSize++;
        frame++;
      } else {
        // Resetting the next sequence number if we don't receive an ack from the server
        // This should help resending all the packets in the current frame
        nextSequenceNo = frame;
      };
      
    }
    
    System.out.println("All the data has been sent. The program is exiting. Bye bye...");
    clientSocket.close();
  }

  // Receiving from the packet to the server
  private boolean receivePacket() throws IOException {
    try{
      System.out.println("Receiving packet from server");
      receivePacket = new DatagramPacket(receiveData, receiveData.length);
      clientSocket.receive(receivePacket);
      return true;
    } catch( SocketTimeoutException exception ){
      // Reducing the sequence number to what it was as there was an error
      System.out.println("There was a socket timeout.");
      return false;
    }
  }

  // Sending the packet to the server
  private void sendPacket() throws IOException {
    System.out.println("Sending packet to server with data: " + new String(sendData));
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
    clientSocket.send(sendPacket);
  };

   // Retreving the sequence number from the data that has been sent to the client
  private void setSequenceNo() {
    String data = new String(receivePacket.getData());
    String snFromData = data.split("Sequence no:")[1].trim();
    sequenceNo = Integer.parseInt(snFromData);
  }

  private void getUserInput() throws IOException { 
    System.out.println("Please enter the hostname");
    BufferedReader hostnameFromUser = new BufferedReader(new InputStreamReader(System.in));
    IPAddress = InetAddress.getByName(hostnameFromUser.readLine());
    System.out.println("The IP address set is: " + IPAddress);
    System.out.println("Please enter your port");
    BufferedReader portFromUser = new BufferedReader(new InputStreamReader(System.in));
    port = Integer.parseInt(portFromUser.readLine());
    System.out.println("The port set is: " + port); // Server is set to 9876
  }

  // Building the data to send to the server which is a combination of the file contents and the sequence number
  private void buildPayload() throws Exception {
    String sentence = new String(Files.readAllBytes(Paths.get(FILEPATH))) + ". Sequence no: ";
    sendData = joinByteArray(sentence.getBytes(), intToByteArray(sequenceNo));
  }

  // Converting int to byte array
  private byte[] intToByteArray( final int i ) {  
    return Integer.toString(i).getBytes();
  }

  private byte[] joinByteArray(byte[] byte1, byte[] byte2) {
    return ByteBuffer.allocate(byte1.length + byte2.length)
      .put(byte1)
      .put(byte2)
      .array();
  }

  public static void main(String args[]) throws Exception {
    // Instanstiating the sender
    Client client = new Client();
    client.goBackN(); 
  }
}
