package lab_tcp;


import java.io.Serializable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author user
 */
public class Message extends Object implements Serializable {
    
    private String message;
    private String user;

    public Message(String user, String message) {
        this.message = message;
        this.user = user;
    }

    public Message() {
        this("", "");
    }
    
    public Message(Message other) {
        this.message = other.message;
        this.user = other.user;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(String user) {
        this.user = user;
    }
    
    public void set(Message m) {
        this.message = m.message;
        this.user = m.user;
    }
    
    public String getMessage() {
        return message;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "[" + user + "] " + message;
    }
}
