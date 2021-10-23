// CMN Summative Assessment 2021
// --------Server-----------
// Communication using Stop-and-Wait protocol
// @version 10/10/2021

import java.io.*;
import java.net.*;
import java.util.*;

class Server {
  private static final int PORT = 9876;
  private static final int BUFFER = 1024;
  private static byte[] sendData = new byte[BUFFER];
  private static byte[] receiveData = new byte[BUFFER];
  private static InetAddress IPAddress;
  private static int port;
  private static DatagramPacket receivePacket;
  private static DatagramSocket serverSocket;
  private static String sentence;

  public void stopAndWait() throws Exception {
    serverSocket = new DatagramSocket(PORT);

    // Keep connection alive forever
    while(true) {
      receivePacket();
      setIPAndPort();
      
      Random random = new Random();
      int chance = random.nextInt( 100 );
      
      // For testing purposes there is a  1 in 2 chance of responding to the message
      if( ((chance % 2) == 0) ){
        sendPacket();
      } else {
        System.out.println( "Oh no! The packet with sequence number '"+ sentence + "' was dropped");
      }
    }
  }

  private static void sendPacket() throws IOException {
    // Sending the bytes back to the client
    sendData = sentence.getBytes();
    System.out.println("Sending packet back to the client: " + sentence);
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
    serverSocket.send(sendPacket);
  }

  private static void receivePacket() throws IOException {
    receivePacket = new DatagramPacket(receiveData, receiveData.length);
    serverSocket.receive(receivePacket);
    sentence = new String(receivePacket.getData());
    System.out.println("Receiving packet with data: " + sentence);
  }

  private static void setIPAndPort() {
    // Setting the IP and port
    IPAddress = receivePacket.getAddress();
    port = receivePacket.getPort();
    System.out.println("Set port: " + port + " and IP address: " + IPAddress);
  }

  public static void main(String args[]) throws Exception {
    Server server = new Server();   //object of sender
    server.stopAndWait(); 
  }
}
