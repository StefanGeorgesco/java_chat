package fr.sgo.app;

import javax.management.NotificationEmitter;

public interface AppMBean extends NotificationEmitter {
	
	public String userName();
	
	public int numberOfCorrespondents();
	
	public int numberOfPairedCorrespondents();
	
	public int numberOfUnpairedCorrespondents();
	
	public int numberOfOnlineCorrespondents();
	
	public int numberOfChats();
	
	public int numberOfCorrespondentChats();
	
	public int numberOfHostedGroupChats();
	
	public int numberOfRemoteGroupChats();
	
	public String correspondents();
	
	public String chats();
	
	public String messages(String chatId);
}
