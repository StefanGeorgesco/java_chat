package fr.sgo.service;

import java.io.Serializable;

import javax.swing.JOptionPane;

/**
 * Class ProfileInfo.
 * Keeps profile info
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
public class ProfileInfo implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5947105262373009381L;
	private boolean set;
    private String userId;
    private String userName;
    private String host;
    private int RMIPort;
    private int JMSPort;
    private static ProfileInfo instance;
    private static final String objectName = "profileinfo";

    private ProfileInfo() {
        set = false;
    }

    public static synchronized ProfileInfo getInstance() {
        if (instance == null)
            instance = (ProfileInfo) Storage.restore(objectName);
        if (instance == null)
            instance = new ProfileInfo();
        if (!instance.isSet()) {
            String name = "";
            while (name.length() == 0) {
                name = JOptionPane.showInputDialog("Nom");
                if (name == null)
                	System.exit(1);
            }
            instance.set(name);
        }
        assert instance.getUserId() != null; // DEBUG
        instance.setHost("localhost");
        instance.setRMIPort(1199);
        instance.setJMSPort(1099);
        return instance;
    }

    public void set(String userName) {
        this.userName = userName;
        userId = IDGenerator.newId();
        set = true;
        Storage.save(this, objectName);
    }

    public void unset() {
        userName = null;
        userId = null;
        set = false;
        Storage.save(this, objectName);
    }

    public boolean isSet() {
        return set;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getHost() {
        return host;
    }

    public int getRMIPort() {
        return RMIPort;
    }
    
    public int getJMSPort() {
    	return JMSPort;
    }

    public void setUserId(String userId) {
         this.userId = userId;
    }

    public void setUserName(String userName) {
    	this.userName = userName;
    }

    public void setHost(String host) {
    	this.host = host;
    }

    public void setRMIPort(int RMIPort) {
    	this.RMIPort = RMIPort;
    }
    
    public void setJMSPort(int JMSPort) {
    	this.JMSPort = JMSPort;
    }

}
