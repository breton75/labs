package lab_rmi;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;

public class Client {

    private final Registry registry;
    private final Map<String, String>methods;

    private Client() throws RemoteException {
        
        // получаем реестр на удаленной машине (в данном случае на локальной)
        registry = LocateRegistry.getRegistry("localhost", Server.PORT);
        
        // получаем список методов интерфейса DateTime
        methods = new HashMap<>();
        for(Method m: DateTime.class.getMethods())
            methods.put(m.getName().toLowerCase(), m.getName());
    }

    public static void main(String[] args) {
        
        try(Scanner scanner = new Scanner(System.in)) {
            
            Client client = new Client();
            
            System.out.println("Available methods:");
            client.methods.forEach((s1, s2) -> System.out.println(s2));
                
            while (true) {
                
                System.out.print("> ");
                
                try {

                    // ждем ввода имени метода
                    String method_name = scanner.nextLine().toLowerCase();
                    if (!client.methods.containsKey(method_name)) {
                        
                        System.err.println("Unknown method: " + method_name);
                        continue;
                    }

                    // получаем метод интерфейса DateTime по введенному имени
                    Method method = DateTime.class.getMethod(client.methods.get(method_name));

                    // получаем ссылку на удаленный объект класса DateTimeImpl
                    DateTime dateTimeStub = (DateTime)client.registry.lookup(DateTimeImpl.NAME);
                    
                    // получаем результат выполнения метода на удаленном объекте
                    Object result = method.invoke(dateTimeStub);
                    
                    // выводим результат
                    System.out.println("< " + result);
                    
                    if (method_name.equals("stop")) {
                        System.out.println("Stopping...");
                        break;
                    }
                    
                } catch (NoSuchMethodException nome) {
                    System.err.println(nome.getMessage());
                }
                    
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
