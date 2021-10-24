// CMN Summative Assessment 2021
// --------Server-----------
// Communication using Go-Back-N protocol
// @version 10/10/2021

import java.io.*;
import java.net.*;
import java.util.*;

class Server {
  private final int PORT = 9876;
  private final int BUFFER = 1024;
  private final int WINDOW_SIZE = 5;
  private byte[] sendData = new byte[BUFFER];
  private byte[] receiveData = new byte[BUFFER];
  private InetAddress IPAddress;
  private int port, sequenceNo;
  private DatagramPacket receivePacket;
  private DatagramSocket serverSocket;
  private String sentence;
  private static ArrayList<DatagramPacket> packetsToSend;

  public void goBackN() throws Exception {
    serverSocket = new DatagramSocket(PORT);

    // Keep connection alive forever
    while(true) {
      receivePacket();
      setIPAndPort();
      setSequenceNo();

      Random random = new Random();
      int chance = random.nextInt( 100 );
      
      // For testing purposes there is a 1 in 5 chance of not responding to the message
      if( !((chance % 5) == 0) ){
        // Sending the bytes back to the client
        storePacket();
        showPacketSequenceNumbers();
        sendPacket();
      } else {
        System.out.println("Oh no! The packet with sequence number '"+ sequenceNo + "' was dropped");
      }
    }
  }

  // Sending the packet back to the client
  private void sendPacket() throws IOException {
    if(!(packetsToSend.size() == WINDOW_SIZE)) return;
    DatagramPacket packetToSend = packetsToSend.get(0);
    int snFromPacket = getSequenceNo(packetToSend);
    System.out.println("Sending back packet to client with sequence no: " + snFromPacket);
    serverSocket.send(packetToSend);
    System.out.println("Packet with sequence no: " + snFromPacket + " has been dropped.");
    packetsToSend.remove(0);
  }

  // Storing the packet received from the client in the array of packets waiting to be sent to the client
  private void storePacket() {
    if(packetsToSendOrdered()) {
      String ack = "ack. Sequence no:" + sequenceNo;
      sendData = ack.getBytes();
      System.out.println("Adding packet to array with ack: " + ack);
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
      packetsToSend.add(sendPacket);
    } else {
      System.out.println("Packets are not ordered");
    }
  }

  // Checking if the next packet that is going to be is correctly the one after the last packet
  private boolean packetsToSendOrdered() {
    if(packetsToSend.size() == 0) return true;
    DatagramPacket lastPacket = packetsToSend.get(packetsToSend.size() - 1);
    int lastSequenceNo = getSequenceNo(lastPacket);
    return sequenceNo == lastSequenceNo + 1;
  }

  // Helper method to show all the sequence numbers waiting to be sent to the client
  private void showPacketSequenceNumbers() {
    ArrayList<Integer>packetNumbers = new ArrayList<Integer>();
    for (DatagramPacket packet : packetsToSend) {
      packetNumbers.add(getSequenceNo(packet));
    };
    System.out.println("The packets that are currently waiting to be sent are: " + Arrays.toString(packetNumbers.toArray()));
  }

  // Receiving from the client
  private void receivePacket() throws IOException {
    receivePacket = new DatagramPacket(receiveData, receiveData.length);
    serverSocket.receive(receivePacket);
    sentence = new String(receivePacket.getData());
    System.out.println("Receiving packet with data: " + sentence);
  }

  // Setting the IP and port
  private void setIPAndPort() {
    IPAddress = receivePacket.getAddress();
    port = receivePacket.getPort();
  }

  // Retreving the sequence number from the data that has been sent to the server
  private void setSequenceNo() {
    String snFromSentence = sentence.split("Sequence no:")[1].trim();
    sequenceNo = Integer.parseInt(snFromSentence);
  }

  // Getting the sequence number from any packet
  private int getSequenceNo(DatagramPacket packet) {
    String data = new String(packet.getData());
    String snFromSentence = data.split("Sequence no:")[1].trim();
    return Integer.parseInt(snFromSentence);
  }

  public static void main(String args[]) throws Exception {
    System.out.println("Waiting for Connection....");
    packetsToSend = new ArrayList<DatagramPacket>();
    // Instanstiating the receiver
    Server server = new Server();
    server.goBackN(); 
  }
}
