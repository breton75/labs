package lab_tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
//import java.net.SocketException;
import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author SVIRIDOV
 **/

public class Server implements AutoCloseable {

    public enum Operations { LOGON, EXIT }

    static final int LISTEN_PORT = 12345;

    private ServerSocket socket;

    private ConcurrentHashMap<String, ArrayList<String>> history;

    private Server(int port) throws IOException {
        
        try {
            
            socket = new ServerSocket(port);
            history = new ConcurrentHashMap<String, ArrayList<String>>();
            
        }
        
        catch(Exception e) {
            System.err.println(e.getMessage());
        }
        

    };
    
    private Runnable UserHandler(Socket socket) {
        
        return () -> {
            
            // создаем потоки чтения и записи
            try(ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());)
            {
                // в самом начале после подключения клиента, сервер отправляет запрос на логин
                output.writeObject(new Message("server", Operations.LOGON.toString()));
                output.flush();
                
                // ждем ответа клиента с логином
                Message msg_in = (Message) input.readObject();
                
                String current_user = msg_in.getUser();
                String current_message = msg_in.getMessage();
                
                try {
                    
                    // в первом сообщении клиент присылает свое имя.
                    if(current_message.equalsIgnoreCase(Operations.LOGON.toString())) {

                        // есть ли в истории такой пользователь
                        if(!history.containsKey(current_user)) {

                            history.put(current_user, new ArrayList<String>());
                            
                            System.out.println(current_user + " logged in.");
                            
                            output.writeObject(new Message("server", "OK"));
                            output.flush();
                        }
                        else 
                            throw new Exception("Refused. This user name already logged.");
                        
                    } else if (current_message.equalsIgnoreCase(Operations.EXIT.toString())) {
                        
                        throw new Exception("Closing connection");
                    }
                    else
                        throw new Exception("Wrong message");
                }
                catch (Exception e) {
                    
                    output.writeObject(new Message("server", e.getMessage()));
                    output.flush();
                    socket.close();

                    return;
                }
                
                // после удачного логина начинаем цикл общения с клиентом, пока тот не закроет сессию
                while(true) {
                    
                    try {

                        // ждем ответа клиента с логином
                        Message m = (Message) input.readObject();
                        System.out.println(m.toString());
                        
                        if(m.getMessage().equalsIgnoreCase(Operations.EXIT.toString())) {

                            output.writeObject(new Message("server", Operations.EXIT.toString()));
                            output.flush();
                                
                            history.get(m.getUser()).clear();
                            history.remove(m.getUser());
                            
                            System.out.println(current_user + " logged out.");
                            
                            socket.close();
                            break;
                            
                        }else {
                            output.writeObject(new Message("server", m.toString() + ": OK"));  
                            output.flush();
                                
                        }
                    }
                    catch(IOException e) {
                                
                    }
                }
            }
            catch (Exception e) {
                
            }
        };
    };

    public static void main(String[] args) {
        
        System.out.println("Server started.");
        try (Server server = new Server(LISTEN_PORT)) {
            
            while(!server.socket.isClosed()) {
                
                Socket client = server.socket.accept();

                new Thread(server.UserHandler(client)).start();
            
            }            
            
        } catch (Exception ex){
            System.err.println(ex.getMessage());
        }
    }
    
    @Override
    public void close() throws Exception {
        if(socket != null && !socket.isClosed())
            socket.close();
        
//        if(server != null && !server.isClosed())
//            server.close();
    }
}
