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
import java.util.Date;
//import java.util.List;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author SVIRIDOV
 **/

public class Server implements AutoCloseable {

    public enum Operations { LOGIN, DATE, REG, EXIT, CLOSE }

    static final int LISTEN_PORT = 12345;

    private ServerSocket socket;

//    private ConcurrentHashMap<String, ArrayList<String>> history;
    private ArrayList<String> logins;
    private ArrayList<Registration> registration;

    private Server(int port) throws IOException {
        
        try {
            
            socket = new ServerSocket(port);
//            history = new ConcurrentHashMap<String, ArrayList<String>>();
            logins = new ArrayList<>();
            registration = new ArrayList<>();
            
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
                output.writeObject(new Message("server", Operations.LOGIN.toString()));
                output.flush();
                
                // ждем ответа клиента с логином
                Message msg_in = (Message) input.readObject();
                
                String current_user = msg_in.getUser();
                String current_message = msg_in.getMessage();
                
                try {
                    
                    // в первом сообщении клиент присылает свое имя.
                    if(current_message.equalsIgnoreCase(Operations.LOGIN.toString())) {

                        // есть ли в истории такой пользователь
                        if(!logins.contains(current_user)) {

                            logins.add(current_user);
                            registration.add(new Registration(new Date(), current_user));
                            
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

                        // ждем запрос от клиента
                        Message m = (Message) input.readObject();
                        System.out.println(m.toString());
                        
                        if(m.getMessage().equalsIgnoreCase(Operations.EXIT.toString()) ||
                                m.getMessage().equalsIgnoreCase(Operations.CLOSE.toString())) {

                            output.writeObject(new Message("server", m.getMessage().toUpperCase()));
                            output.flush();
                            
                            logins.remove(m.getUser());
                            
                            System.out.println(current_user + " logged out.");
                            
                            socket.close();
                            break;
                            
                        } else if(m.getMessage().equalsIgnoreCase(Operations.DATE.toString())) {

                            output.writeObject(new Message("server", "Current Date/Time: " + new Date().toString()));
                            output.flush();
                            
                            System.out.println(Operations.DATE.toString());                            
                            
                        } else if (m.getMessage().equalsIgnoreCase(Operations.REG.toString())) {
                            
                            String registration_list = "Registration history for " + current_user + ":\n";
                            for(Registration r : registration)
                                if(r.getUser().equalsIgnoreCase(current_user))
                                    registration_list += r.getDate().toString() + '\n';
                            
                            output.writeObject(new Message("server", registration_list));
                            output.flush();
                            
                            System.out.println(Operations.REG.toString());   
                            
                        }        
                        else {
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

    }
}
