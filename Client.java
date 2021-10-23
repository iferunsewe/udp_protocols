// CMN Summative Assessment 2021
// --------Client-----------
// Communication using Stop-and-Wait protocol
// @version 10/10/2021

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.ByteBuffer;

class Client {
	private static final int SN = 1;
  private static final int BUFFER = 1024;
  private static final int SLIDING_WINDOW_SIZE = 5;
  private static final int UNACKNOWLEDGED_MESSAGES_SIZE = 0;
  private static final String FILEPATH = "umbrella.txt";
  private static InetAddress IPAddress;
  private static int port;
  private static byte[] sendData = new byte[BUFFER];
  private static byte[] receiveData = new byte[BUFFER];
  private static DatagramSocket clientSocket;

  public void stopAndWait() throws Exception {
    // Creating a socket
    clientSocket = new DatagramSocket();
    clientSocket.setSoTimeout( 1000 );
		Integer sequenceNumber = SN;
    Integer unackMessagesSize = UNACKNOWLEDGED_MESSAGES_SIZE;
    
    getUserInput();

    // Used to demo
    for (int counter = 0; counter < 10; counter++) {
			boolean timeOut = true;

			while(timeOut && unackMessagesSize<=SLIDING_WINDOW_SIZE){
        sequenceNumber++;
        System.out.println("Increasing the sequence number to: " + sequenceNumber);
        buildPayload(sequenceNumber);

        try{
          sendPacket();
          receivePacket();
          // Received an ack so the loop can stop
          timeOut = false;
          unackMessagesSize--;
        } catch( SocketTimeoutException exception ){
          // Reducing the sequence number to what it was as there was an error
					sequenceNumber--;
          System.out.println("There was a timeout. Reducing the sequence number to: " + sequenceNumber);

          unackMessagesSize++;
        }
      }
    }
    clientSocket.close();
  }

  private void receivePacket() throws IOException {
    // Receiving from the packet to the server
    System.out.println("Receiving packet");
    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    clientSocket.receive(receivePacket);
    // Unpacking the data and printing it
    String ack = new String(receivePacket.getData());
    System.out.println("From server:  " + ack);
  }

  private void sendPacket() throws IOException {
    // Sending the packet to the server
    System.out.println("Sending packet with data: " + new String(sendData));
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

  private void buildPayload(int sequenceNumber) throws Exception {
    String sentence = new String(Files.readAllBytes(Paths.get(FILEPATH))) + ". Sequence no: ";
    sendData = joinByteArray(sentence.getBytes(), intToByteArray(sequenceNumber));
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
    Client client = new Client();   //object of sender
    client.stopAndWait(); 
  }
}