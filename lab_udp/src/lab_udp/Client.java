package lab_udp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Scanner;
import lab_udp.Message.Command;

public class Client implements AutoCloseable {

    private DatagramPacket packet;
    private final DatagramSocket socket;
    private final Scanner scanner;

    private Client() throws SocketException {
        socket = new DatagramSocket();
        scanner = new Scanner(System.in);
    }

    @Override
    public void close() {
        socket.close();
    }

    private void sendMessage(Command command, String message, InetAddress ip, int port) {

        try (ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
             ObjectOutputStream obj_stream = new ObjectOutputStream(byte_stream))
        {
            obj_stream.writeObject(new Message(command, message, ip, port));
            obj_stream.flush();
            
            byte[] bytes = byte_stream.toByteArray();
            
            socket.send(new DatagramPacket(bytes, bytes.length, ip, port));

        } catch (IOException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {

        try (Client client = new Client()) {

            while (true) {

                System.out.print("Enter message > ");
                System.out.flush();

                try {
                    String[] input = client.scanner.nextLine().trim().split(":", 2);

                    Command cmd;
                    String msg;
                    
                    switch(input.length) {
                        
                        case 1:
                            if(input[0].toUpperCase().equals(Command.CLIENT_MESSAGE.toString()))
                                cmd = Command.CLIENT_MESSAGE;
                            else if(input[0].toUpperCase().equals(Command.PING.toString()))
                                cmd = Command.PING;
                            else if(input[0].toUpperCase().equals(Command.TOP_MESSAGE.toString()))
                                cmd = Command.TOP_MESSAGE;
                            else
                                throw new Exception("Unknown command");
                            
                            msg = "";
                            
                            break;
                            
                        case 2:
                        {
                            if (input[0].toUpperCase().equals(Command.ADD.toString())) {
                                
                                cmd = Command.ADD;
                                msg = input[1];
                            }
                            else
                                throw new Exception("Unknown command");
                            
                            break;
                        }
                        
                        default:
                            continue;
                        
                    }
                    
                    client.sendMessage(cmd,
                                msg,
                                InetAddress.getLoopbackAddress(),
                                Server.LISTEN_PORT);

                    client.packet = new DatagramPacket(new byte[Server.BUFFER_SIZE], Server.BUFFER_SIZE);
                    client.socket.receive(client.packet);
                    
                    System.out.println("Got response: " + new String(client.packet.getData()).trim());

                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            }

        } catch (IOException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }
}
