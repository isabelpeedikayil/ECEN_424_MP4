import java.net.*;
import java.util.Random;
import java.util.Scanner;

public class Listener {
    public static void main(String[] args){
        try{
            // input messages to get values
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the port for the Listener:");
            int listener_port = scanner.nextInt();

            // prep the socket to listen
            DatagramSocket socket = new DatagramSocket(listener_port);

            // create a buffer that holds incoming data packets
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // receive the first message which has to be message 0
            socket.receive(packet);
            String message0 = new String(packet.getData()).trim();
            String[] parts = message0.split(":");
            int total_messages = Integer.parseInt(parts[1]);
            System.out.println("Total messages to receive: " + total_messages);

            InetAddress talkerAddress = packet.getAddress();
            int talker_port = packet.getPort();

            // send an ACK for initial message
            String ack0 = "ACK:0";
            socket.send(new DatagramPacket(ack0.getBytes(), ack0.length(), talkerAddress, talker_port));

            StringBuilder received_data = new StringBuilder();
            int expected_SeqNum = 1;
            Random random = new Random(); // simulates the random ack drops

            while(expected_SeqNum <= total_messages){
                socket.receive(packet);
                // extracts data
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received: " + message);

                // parse the message to extract the sequence number and data
                parts = message.split(":");
                int seqNum = Integer.parseInt(parts[0]);
                String data = parts[1];

                if(seqNum == expected_SeqNum){
                    // simulates random ack dropping
                    if(random.nextBoolean()){
                        String ack = "ACK:" + seqNum;
                        socket.send(new DatagramPacket(ack.getBytes(), ack.length(), talkerAddress, talker_port));
                        System.out.println("ACK Sent for SeqNum: " + seqNum);
                        
                        received_data.append(data);
                        expected_SeqNum++;
                    }
                    else{
                        System.out.println("Dropped ACK for SeqNum: " + seqNum);
                    }
                } else{
                    System.out.println("Ignored message with SeqNum: " + seqNum);
                }
            }
            // print concatenated string 
            if(expected_SeqNum > total_messages){
                    System.out.println("All messages have been received: " + received_data.toString());
            } else{
                System.out.println("Failed to receive all messages.");
            }
            // close the socket and throw exceptions
            socket.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
