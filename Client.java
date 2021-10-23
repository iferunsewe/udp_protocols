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
  private static DatagramPacket receivePacket;
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
          setSequenceNo();
          
          // If the server has changed the sequence number to the expected sequence number then
          // the loop can stop and we update the expected sequence number so the next packet can
          // be sent
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

  // Receiving from the packet to the server
  private void receivePacket() throws IOException {
    System.out.println("Receiving packet from server");
    receivePacket = new DatagramPacket(receiveData, receiveData.length);
    clientSocket.receive(receivePacket);
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

  // Updating the expected sequence number to either 0 or 1
  private int updateExpectedSequenceNo() {
    return expectedSequenceNo = (expectedSequenceNo==0) ? 1 : 0;
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
  private void buildPayload(int sequenceNo) throws Exception {
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
    client.stopAndWait(); 
  }
}
