import java.net.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

public class Listener {
    public static void main(String[] args){
        try{
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the port for the listener:");
            int listener_port = scanner.nextInt();

            // prep the socket 
            DatagramSocket socket = new DatagramSocket(listener_port);

            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            socket.receive(packet);
            String message0 = new String(packet.getData()).trim();
            String[] parts = message0.split(":");
            int total_messages = Integer.parseInt(parts[1]);
            System.out.println("Total messages to receive: " + total_messages);

            InetAddress talkerAddress = packet.getAddress();
            int talker_port = packet.getPort();

            String ack0 = "ACK:0";
            socket.send(new DatagramPacket(ack0.getBytes(), ack0.length(), talkerAddress, talker_port));

            StringBuilder received_data = new StringBuilder();
            int expected_SeqNum = 1;
            Random random = new Random();

            //boolean all_messages_received_correctly = true;
            //for (int i = 1; i <= total_messages; i++){
            while(expected_SeqNum <= total_messages){
                //boolean received = false;
                //while(!received){
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received: " + message);

                parts = message.split(":");
                int seqNum = Integer.parseInt(parts[0]);
                String data = parts[1];

                    //if(random.nextBoolean()){
                HashSet<Integer> processedSeqNums = new HashSet<>();
                if(seqNum == expected_SeqNum){
                    if(random.nextBoolean()){
                        String ack = "ACK:" + seqNum;
                        socket.send(new DatagramPacket(ack.getBytes(), ack.length(), talkerAddress, talker_port));
                        System.out.println("ACK Sent for SeqNum: " + seqNum);
                        
                        received_data.append(data);
                        expected_SeqNum++;
                        processedSeqNums.add(seqNum);
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
            socket.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }