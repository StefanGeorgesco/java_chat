package fr.sgo.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface RMIService
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
public interface RMIService extends Remote {

    public boolean isActive() throws RemoteException;
    
    public ProfileInfo getProfileInfo() throws RemoteException;
    
    public void requestPairing(RMIService service, String inId) throws RemoteException;
    
    public void acceptPairingRequest(RMIService service, String inId, String outId) throws RemoteException;
    
    public void refusePairing(RMIService service, String inId) throws RemoteException;
    
    public String getDestinationName(RMIService service, String outId) throws RemoteException;

}
