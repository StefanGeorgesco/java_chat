package fr.sgo.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface ServiceRMI
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
public interface ServiceRMI extends Remote {

    public boolean isActive() throws RemoteException;
    
    public ProfileInfo getProfileInfo() throws RemoteException;
    
    public void requestPairing(ServiceRMI service, String inId)  throws RemoteException;
    
    public void acceptPairingRequest(ServiceRMI service, String inId, String outId) throws RemoteException;
    
    public void refusePairing(ServiceRMI service, String inId)  throws RemoteException;

}
