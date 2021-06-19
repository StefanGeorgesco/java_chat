package fr.sgo.entity;

import java.io.Serializable;
import java.util.Observable;

import fr.sgo.service.IDGenerator;

/**
 * Class Correspondent
 * 
 * Represents a conversation correspondent
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class Correspondent extends Observable implements Serializable, Comparable<Correspondent> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5648639457898214832L;
	public static final int UNPAIRED = 0;
	public static final int PAIRING_REQUEST_SENT = 1;
	public static final int PAIRING_REQUEST_RECEIVED = 2;
	public static final int PAIRED = 3;
	private String userId;
	private String userName;
	private boolean online;
	private PairingInfo pairingInfo;

	public Correspondent(String userId, String userName, boolean online) {
		this.userId = userId;
		this.userName = userName;
		this.online = online;
		this.pairingInfo = new PairingInfo();
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

	public PairingInfo getPairingInfo() {
		return this.pairingInfo;
	}

	public boolean isPaired() {
		return this.pairingInfo.getPairingStatus() == PAIRED;
	}

	@Override
	public String toString() {
		return userName + ", userId=" + getUserId() + ", " + (isOnline() ? "online" : "offline") + ", "
				+ (isPaired() ? "paired" : "unpaired");
	}

	public class PairingInfo implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6527180419326981253L;
		private int pairingStatus;
		private String outId;
		private String inId = null;

		public PairingInfo() {
			this.outId = IDGenerator.newId();
			this.pairingStatus = UNPAIRED;
		}

		public int getPairingStatus() {
			return pairingStatus;
		}

		public String getOutId() {
			return outId;
		}

		public String getInId() {
			return inId;
		}

		public void setPairingStatus(int status) {
			synchronized (this) {
				this.pairingStatus = status;
			}
			Correspondent.this.setChanged();
			Correspondent.this.notifyObservers();
		}

		public synchronized void setInId(String id) {
			synchronized (this) {
				this.inId = id;
			}
		}
	}

	@Override
	public int compareTo(Correspondent correspondent) {
		return new String(userName).compareTo(correspondent.getUserName());
	}
}
