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
//    private ObjectOutputStream output;
//    private ObjectInputStream input;

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
                    boolean logged = false;
                    boolean closed = false;
                    
                    // получаем запрс сервера на логон
                    Message m1 = (Message) input.readObject();
                    System.out.println(m1.toString());
                    
                    String current_user = "";                                    
                    
                    if(m1.getUser().equalsIgnoreCase("server") &&
                       m1.getMessage().equalsIgnoreCase(Server.Operations.LOGIN.toString())) {
                        
                        // данное имя пользователя будет использоваться во всех дальнейших сообщениях
                        current_user = client.scanner.nextLine();
                        
                        output.writeObject(new Message(current_user, Server.Operations.LOGIN.toString()));
                        output.flush();
                        
                        // ждем результат авторизации
                        Message m2 = (Message) input.readObject();
                        System.out.println(m2.toString());
                        
                        logged = m2.getMessage().equalsIgnoreCase("OK");
                            
                    }
                    
                    while(logged && !closed) {
                        
                        output.writeObject(new Message(current_user, client.scanner.nextLine()));
                        output.flush();
                        
                        Message m3 = (Message)input.readObject();
                        System.out.println(m3.toString());
                        
                        logged = !m3.getMessage().equalsIgnoreCase(Server.Operations.EXIT.toString());
                        closed = m3.getMessage().equalsIgnoreCase(Server.Operations.CLOSE.toString());
                    }

                    client.close();
                    
                    if(closed)
                        break;
                    
            } catch (Exception ex) {
                
                System.err.println(ex.getMessage());
            }
        }
            
        
            
//        } catch (ClassNotFoundException | IOException ex) {
//            ex.printStackTrace();
//        }
    }
}
