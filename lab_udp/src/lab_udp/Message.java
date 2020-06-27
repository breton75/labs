package lab_udp;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;

public class Message implements Serializable {

    enum Command { ADD, TOP_MESSAGE, CLIENT_MESSAGE, PING }

    private static int last = -1;

    private int userid;
    private Command command;
    private String message;
    private Date date;
    private InetAddress ip;
    private int port;

    Message(Command command, String message, InetAddress ip, int port) {
        this.command = command;
        this.message = message;
        date = new Date();
        this.ip = ip;
        this.port = port;
    }

    Message(Message m) {
        this.userid = m.userid;
        this.command = m.command;
        this.message = m.message;
        this.date = m.date;
        this.ip = m.ip;
        this.port = m.port;
    }

    static int getLast() {
        return last;
    }

    Command getCommand() {
        return command;
    }

    void setId(int userid) {
        this.userid = userid;
    }

    public int getUserid() {
        return userid;
    }

    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return date + ": " + ip + ":" + port + " -> " + message;
    }
}
