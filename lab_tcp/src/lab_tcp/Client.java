package lab_tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Scanner;

public class Client implements AutoCloseable {

    private Scanner scanner;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    private Client(InetAddress address, int port) throws IOException {
        scanner = new Scanner(System.in);
        socket = new Socket(address, port);
//        output = new ObjectOutputStream(socket.getOutputStream());
//        input = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    public static void main(String[] args) {
        
        while(true) {
            
            try (Client client = new Client(InetAddress.getLoopbackAddress(), Server.LISTEN_PORT);
                 ObjectOutputStream output = new ObjectOutputStream(client.socket.getOutputStream());
                 ObjectInputStream input = new ObjectInputStream(client.socket.getInputStream()))
                {

    System.out.println("1");
    //                ObjectInputStream input = new ObjectInputStream(client.socket.getInputStream());
                    Message m = new Message((Message) input.readObject());
                    System.out.println("2");
                    System.out.println(m.toString());

    //                ObjectOutputStream output = new ObjectOutputStream(client.socket.getOutputStream());
                    output.writeObject(new Message("admin", client.scanner.nextLine()));

    //            }

            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
            
        }
            
//        } catch (ClassNotFoundException | IOException ex) {
//            ex.printStackTrace();
//        }
    }
}
