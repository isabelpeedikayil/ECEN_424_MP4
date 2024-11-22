import java.net.*;
import java.util.Scanner;

public class Talker {
    public static void main(String[] args){
        try{
            // output and input messages to get variable values
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the IP address for the Listener");
            String listener_IP = scanner.nextLine();
            System.out.println("Enter the port for the Listener");
            int listener_port = scanner.nextInt();
            System.out.println("Enter the port for the Talker");
            int talker_port = scanner.nextInt();
            scanner.nextLine();
            System.out.println("Enter a string of up to 50 characters:");
            String input_string = scanner.nextLine();
            
            // string error detector because it has to be less than 50 characters and has to have a value
            if (input_string.length() > 50){
                System.out.println("Error. The input string entered is more than 50 characters!");
                return;
            }

            if (input_string.length() < 1){
                System.out.println("No message to send. Exiting...");
                return;
            }

            // the entered string is to be broken up into 5 messages of 10 characters 
            String[] messages = new String[(input_string.length() + 9) /10];
            int count = 0;
            for (int i = 0; i < messages.length; i++){
                StringBuilder message_portion = new StringBuilder();
                for (int j = 0; j < 10 && count < input_string.length(); j++){
                    message_portion.append(input_string.charAt(count));
                    count++;

                }
                messages[i] = message_portion.toString();
            }

            // prep the socket
            try{
                DatagramSocket socket = new DatagramSocket(talker_port);
                InetAddress listenerAddress = InetAddress.getByName(listener_IP);

            
            // talker should send each message (starting with 0)
            String message0 = "0:" + messages.length;
            byte[] buffer = message0.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, listenerAddress, listener_port);
            socket.send(packet);

            boolean all_messages_sent_correctly = true;
            // send messages with the stop and wait protocol
            for (int i = 0; i < messages.length; i++){
                boolean ack_recieved = false;
                // max retries is error handling for infinite loop 
                int retries = 0;
                int MaxRetries = 10;

                while(!ack_recieved && retries < MaxRetries){
                    // send message
                    String message = (i+1) + ":" + messages[i];
                    buffer = message.getBytes();
                    packet = new DatagramPacket(buffer, buffer.length, listenerAddress, listener_port);
                    socket.send(packet);
                    System.out.println("Sent: " + message);

                    // wait for ack
                    try{
                        // 2 second timeout
                        socket.setSoTimeout(2000);
                        byte[] ack_buffer = new byte[10];
                        DatagramPacket ack_packet = new DatagramPacket(ack_buffer, ack_buffer.length);
                        socket.receive(ack_packet);
                        String ack = new String(ack_packet.getData()).trim();
                        if(ack.equals("ACK:" + (i + 1))) {
                            System.out.println("Recieved ACK for message" + (i+1));
                            ack_recieved = true;
                        }
                        // if listener doesn't ack a message for 2 seconds, talker should timeout and resend message
                    } catch (SocketTimeoutException e){
                        retries++;
                        System.out.println("Timeout! Resending Message Now!" + (i + 1));
                    }
                }
                
                if(retries >= MaxRetries){
                System.out.println("Failed to send message " + (i + 1) + " after " + MaxRetries + " attempts");
                all_messages_sent_correctly = false;
                break;
            }
        }
            // after recieving all messages, listener must close the connection
            socket.close();
            if(all_messages_sent_correctly){
                System.out.println("All messages sent successfully!");
            }
            else{
                System.out.println("Not all messages were sent successfully");
            }
        } catch (java.net.BindException e){
            System.out.println("Port " + talker_port + " is already in use. Please select a different port.");
        }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
