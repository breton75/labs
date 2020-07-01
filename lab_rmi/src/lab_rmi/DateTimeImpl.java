package lab_rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalTime;

public class DateTimeImpl extends UnicastRemoteObject implements DateTime {

    static final String NAME = "dateTime";

    private Registry registry;

    DateTimeImpl(Registry registry) throws RemoteException {
        this.registry = registry;
    }

    @Override
    public String getDate() throws RemoteException {
        return LocalDate.now().toString();
    }

    @Override
    public String getTime() throws RemoteException {
        return LocalTime.now().toString().substring(0, 8);
    }

    @Override
    public boolean stop() throws RemoteException {
        try {
            
            registry.unbind(NAME);
            UnicastRemoteObject.unexportObject(this, true);
            
            return true;
            
        } catch (NotBoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
