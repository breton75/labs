package lab_rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {

    public static int PORT = 50000;
    
    private Server() throws RemoteException {
        
        // создаем реестр на заданном порту
        Registry registry = LocateRegistry.createRegistry(PORT);
        
        // создаем новый объект DateTimeImpl
        DateTime dateTimeSkelet = new DateTimeImpl(registry);
        
        // регистрируем в реестре объект DateTimeImpl для экспорта
        registry.rebind(DateTimeImpl.NAME, dateTimeSkelet);
        
        System.out.println("Server started");
    }

    public static void main(String[] args) {
        try {
            
            new Server();
            
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
