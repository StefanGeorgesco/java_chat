package fr.sgo.service;

/**
 * Class CorrespondentServiceInfo
 * 
 * Stores correspondent name & RMI info
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
public class CorrespondentServiceInfo
{
    private String userId;
    private String userName;
    private String host;
    private RMIService rmiService;
    
    public CorrespondentServiceInfo(String userId, String userName, String host, RMIService rmiService) {
        this.userId = userId;
        this.userName = userName;
        this.host = host;
        this.rmiService = rmiService;
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
    
    public RMIService getServiceRMI() {
        return rmiService;
    }
}
