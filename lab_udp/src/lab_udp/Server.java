package lab_udp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;


public class Server implements AutoCloseable {

    static final int LISTEN_PORT = 9999;
    static final int BUFFER_SIZE = 2048;

//    private final List<Entry<Integer, Message>> messages;
    private final List<Message> messages;
    private final DatagramSocket socket;
    private DatagramPacket packet;

    private Server() throws SocketException {
        messages = new ArrayList<>();
        socket = new DatagramSocket(LISTEN_PORT);
        packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
    }

    private void run() throws IOException {

        System.out.println("Server started");

        while (true) {

            socket.receive(packet);

            try (ByteArrayInputStream byte_stream = new ByteArrayInputStream(packet.getData());
                 ObjectInputStream obj_stream = new ObjectInputStream(byte_stream)) {

                Message message = new Message((Message) obj_stream.readObject());
                String response = "";

                switch (message.getCommand()) {
                    case ADD:
                        messages.add(message);
                        response = "OK";
                        break;
                        
                    case TOP_MESSAGE:
                        
                        int counter = 0;
                        response = "Last 10 messages:\n"; 
                        for(Message m: messages)                            
                            if(m.getUserid() == message.getUserid() && counter < 10)
                                response += ++counter + ": " + m.getCommand().toString() + ": " + m.getMessage() + "\n";
                        break;
                        
                    case CLIENT_MESSAGE:
                        
                        response = "All messages:\n"; 
                        for(Message m: messages)                            
                            if(m.getUserid() == message.getUserid())
                                response += m.getCommand().toString() + ": " + m.getMessage() + "\n";
                        break;
                        
                    case PING:
                        response = "READY";
                        break;
                        
                    default: // если неизвестаня команда
                        continue;
                }

                socket.send(new DatagramPacket(
                        response.getBytes(), response.getBytes().length, packet.getAddress(), packet.getPort()));

            } catch (Exception ex) {
                System.err.println("Error: " + ex.getMessage());
            }
        }
    }

    @Override
    public void close() {
        if (!socket.isClosed()) {
            socket.close();
        }
    }

    public static void main(String[] args) {

        try (Server server = new Server()) {
            server.run();
        } catch (IOException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }
}
