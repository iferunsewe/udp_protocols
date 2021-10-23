// CMN Summative Assessment 2021
// --------Client-----------
// Communication using Stop-and-Wait protocol
// @version 10/10/2021

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.ByteBuffer;

class Client {
  private static final int BUFFER = 1024;
  private static final String FILEPATH = "umbrella.txt";
  private static InetAddress IPAddress;
  private static int port;
  private static byte[] sendData = new byte[BUFFER];
  private static byte[] receiveData = new byte[BUFFER];
  private static DatagramSocket clientSocket;
  private static int sequenceNo = 0;
  private static int expectedSequenceNo = 1;

  public void stopAndWait() throws Exception {
    // Creating a socket
    clientSocket = new DatagramSocket();
    clientSocket.setSoTimeout( 1000 );
    
    getUserInput();

    // Used to demo
    for (int counter = 0; counter < 10; counter++) {
			boolean timeOut = true;

			while(timeOut){
        buildPayload(sequenceNo);

        try{
          sendPacket();
          receivePacket();
          // Received an ack so the loop can stop
          if(sequenceNo == expectedSequenceNo) {
            System.out.println("Received ack with sequence number: " + sequenceNo);
            updateExpectedSequenceNo();
            timeOut = false;
          }
        } catch( SocketTimeoutException exception ){
          // Reducing the sequence number to what it was as there was an error
          System.out.println("There was a socket timeout.");
        }
      }
    }
    System.out.println("All the data has been sent. The program is exiting. Bye bye...");
    clientSocket.close();
  }

  private void receivePacket() throws IOException {
    // Receiving from the packet to the server
    System.out.println("Receiving packet from server");
    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    clientSocket.receive(receivePacket);
    // Unpacking the data and printing it
    String ack = new String(receivePacket.getData());
    getSequenceNo(ack);
  }

  private void sendPacket() throws IOException {
    // Sending the packet to the server
    System.out.println("Sending packet to server with data: " + new String(sendData));
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
    clientSocket.send(sendPacket);
  };

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

  private void buildPayload(int sequenceNo) throws Exception {
    String sentence = new String(Files.readAllBytes(Paths.get(FILEPATH))) + ". Sequence no:";
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

  private void getSequenceNo(String sentence) {
    String snFromSentence = sentence.split("Sequence no:")[1].trim();
    sequenceNo = Integer.parseInt(snFromSentence);
  }

  private int updateExpectedSequenceNo() {
    return expectedSequenceNo = (expectedSequenceNo==0) ? 1 : 0;
  }

  public static void main(String args[]) throws Exception {
    // Instanstiating the sender
    Client client = new Client();
    client.stopAndWait(); 
  }
}
