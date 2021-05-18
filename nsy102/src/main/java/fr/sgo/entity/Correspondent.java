package fr.sgo.entity;

import java.io.Serializable;
import java.util.Observable;

/**
 * Class Correspondent
 * 
 * Represents a conversation correspondent
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class Correspondent extends Observable implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5648639457898214832L;
	private String userId;
    private String userName;
    private boolean online;
    
    public Correspondent(String userId, String userName, boolean online) {
        this.userId = userId;
        this.userName = userName;
        this.online = online;
    }
    
    public Correspondent(String userId, String userName) {
        this(userId, userName, false);
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public boolean isOnline() {
        return online;
    }
    
    public void setUserName(String userName) {
        synchronized (this) {
            this.userName = userName;
        }
        setChanged();
        notifyObservers();
    }
    
    public void setOnline(boolean online) {
        synchronized (this) {
            this.online = online;
        }
        setChanged();
        notifyObservers();
    }
    
    @Override
    public String toString() {
        return userName + " (" + (isOnline() ? "online" : "offline") + ")";
    }
}
