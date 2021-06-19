package fr.sgo.controller;

import java.rmi.Remote;
import java.rmi.RemoteException;

import fr.sgo.entity.GroupChat;
import fr.sgo.service.ProfileInfo;

/**
 * Interface RMIService
 * 
 * Defines RMI interface for RMIController class
 *
 * @author Stefan Georgesco
 * @version 1.0
 */
public interface RMIService extends Remote {

	public boolean isOnline() throws RemoteException;

	public ProfileInfo getProfileInfo() throws RemoteException;

	public String getDestinationName(RMIService service, String outId) throws RemoteException;

	public void requestPairing(RMIService service, String inId) throws RemoteException;

	public void acceptPairingRequest(RMIService service, String inId, String outId) throws RemoteException;

	public void refusePairing(RMIService service, String inId) throws RemoteException;

	public boolean inviteToGroupChat(RMIService service, String outId, GroupChat chat) throws Exception;

}
