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
    private ServiceRMI serviceRMI;
    
    public CorrespondentServiceInfo(String userId, String userName, String host, ServiceRMI serviceRMI) {
        this.userId = userId;
        this.userName = userName;
        this.host = host;
        this.serviceRMI = serviceRMI;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public ServiceRMI getServiceRMI() {
        return serviceRMI;
    }
}
