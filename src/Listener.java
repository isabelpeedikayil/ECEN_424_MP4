import java.io.*;
import java.net.*;
import java.util.Random;

public class Listener {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: java Listener <listener port> <talker IP> <talker port>");
            return;
        }

        int listenerPort = Integer.parseInt(args[0]);
        String talkerIP = args[1];
        int talkerPort = Integer.parseInt(args[2]);

        DatagramSocket socket = new DatagramSocket(listenerPort);
        InetAddress talkerAddress = InetAddress.getByName(talkerIP);

        StringBuilder receivedMessage = new StringBuilder();
        int totalMessages = -1;

        Random random = new Random();

        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String message = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received: " + message);

            // Extract sequence number and content
            String[] parts = message.split(":", 2);
            int seqNum = Integer.parseInt(parts[0]);
            String content = parts.length > 1 ? parts[1] : "";

            if (seqNum == 0) {
                totalMessages = Integer.parseInt(content);
                System.out.println("Total messages to receive: " + totalMessages);
            } else {
                receivedMessage.append(content);
            }

            // Simulate ACK drop with 50% probability
            if (random.nextBoolean()) {
                String ack = "ACK:" + (seqNum + 1);
                DatagramPacket ackPacket = new DatagramPacket(ack.getBytes(), ack.getBytes().length, talkerAddress, talkerPort);
                socket.send(ackPacket);
                System.out.println("Sent: " + ack);
            } else {
                System.out.println("Simulating ACK drop for sequence: " + seqNum);
            }

            // Check if all messages are received
            if (seqNum == totalMessages) {
                System.out.println("All messages received. Full message: " + receivedMessage);
                break;
            }
        }
        socket.close();
    }
}
