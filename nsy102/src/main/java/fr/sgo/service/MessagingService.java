package fr.sgo.service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.exolab.jms.message.MapMessageImpl;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.InMessage;
import fr.sgo.entity.Message;
import fr.sgo.entity.OutMessage;
import fr.sgo.model.MessageManager;
import fr.sgo.view.InformationMessage;

public class MessagingService {
	private final static boolean T = true; //
	private static MessagingService instance = null;
	private Context context = null;
	private ConnectionFactory factory;
	private Connection connection;
	private static final String factoryName = "ConnectionFactory";
	private Session session;
	private Map<String, MessageProducer> senders;
	private Map<String, MessagingInfo> messagingInfoRecords;
	private App app;

	private MessagingService(App app) {
		this.app = app;
		this.senders = Collections.synchronizedMap(new HashMap<String, MessageProducer>());
		this.messagingInfoRecords = Collections.synchronizedMap(new HashMap<String, MessagingInfo>());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (context != null) {
					try {
						context.close();
					} catch (NamingException ne) {
						ne.printStackTrace();
					}
				}
				if (connection != null) {
					try {
						connection.close();
					} catch (JMSException jmse) {
						jmse.printStackTrace();
					}
				}
			}
		});
	}

	public static MessagingService getInstance(App app) {
		if (instance == null)
			instance = new MessagingService(app);
		return instance;
	}

	public void open() {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
			properties.put(Context.PROVIDER_URL, "tcp://localhost:3035/");
			context = new InitialContext(properties);
			factory = (ConnectionFactory) context.lookup(factoryName);
			connection = factory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			connection.start();
			if (T)
				System.out.println("service jms local installé");
		} catch (NamingException ne) {
			ne.printStackTrace();
			System.exit(1);
		} catch (JMSException jmse) {
			jmse.printStackTrace();
			System.exit(1);
		}

	}

	private javax.jms.Message translateMessage(Message applicationMessage) {
		javax.jms.Message jmsMessage = null;
		try {
			jmsMessage = new MapMessageImpl();
			jmsMessage.setStringProperty("contents", applicationMessage.getContents());
			jmsMessage.setLongProperty("timeWritten", applicationMessage.getTimeWritten());
		} catch (JMSException e) {
			e.printStackTrace();
			new InformationMessage(app, "Le message jms " + applicationMessage.getContents() + " n'a pas pu être créé");
		}
		return jmsMessage;
	}

	private Message translateMessage(javax.jms.Message jmsMessage) {
		Message applicationMessage = null;
		try {
			applicationMessage = new InMessage(jmsMessage.getStringProperty("contents"),
					jmsMessage.getLongProperty("timeWritten"));
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return applicationMessage;
	}

	public void sendMessage(Correspondent correspondent, OutMessage message) {
		if (context == null)
			open();
		String destinationName = correspondent.getPairingInfo().getOutId();
		MessageProducer sender = senders.get(correspondent.getUserId());
		if (sender == null) {
			Destination destination = null;
			try {
				destination = (Destination) context.lookup(destinationName);
				if (T)
					System.out.println("file " + destinationName + " récupérée");
			} catch (NamingException e) {
				try {
					destination = session.createQueue(destinationName);
					if (T)
						System.out.println("file " + destinationName + " créée");
				} catch (JMSException e1) {
					e1.printStackTrace();
					new InformationMessage(app, "La file " + destinationName + " n'a pas pu être créée");
				}
			}
			try {
				sender = session.createProducer(destination);
				if (T)
					System.out.println("producteur de messages sur la file " + destinationName + " créé");
			} catch (JMSException e) {
				e.printStackTrace();
				new InformationMessage(app,
						"Le producteur de message sur la file " + destinationName + " n'a pas pu être créé");
			}
			if (sender != null)
				senders.put(correspondent.getUserId(), sender);
		}
		if (sender != null) {
			try {
				sender.send(translateMessage(message));
				if (T)
					System.out.println("message " + message.getContents() + " envoyé sur la file " + destinationName);
			} catch (JMSException e) {
				e.printStackTrace();
				new InformationMessage(app, "Le message jms " + message.getContents() + " n'a pas pu être envoyé");
			}
		}
	}

	public void setMessageListener(Correspondent correspondent) {
		String userId = correspondent.getUserId();
		CorrespondentServiceInfo correspondentServiceInfo = MessagingService.this.app.getCorrespondentServiceLocator()
				.lookup(userId);
		if (correspondentServiceInfo != null) {
			String host = correspondentServiceInfo.getHost();
			int port = 1199;
//			try {
//				port = correspondentServiceInfo.getServiceRMI().getProfileInfo().getRMIPort();
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
			Hashtable<String, String> props = new Hashtable<String, String>();
			props.put(Context.PROVIDER_URL, "rmi://" + host + ":" + Integer.toString(port) + "/");
			props.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
			props.put(Context.SECURITY_PRINCIPAL, "admin");
			props.put(Context.SECURITY_CREDENTIALS, "openjms");
			Context context = null;
			Connection connection = null;
			MessageConsumer receiver = null;
			try {
				context = new InitialContext(props);
				ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
				connection = factory.createConnection();
				Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				Destination destination = (Destination) context.lookup(correspondent.getPairingInfo().getInId());
				receiver = session.createConsumer(destination);
				receiver.setMessageListener(
						new InMessageHandler(userId, MessagingService.this.app.getMessageManager()));
			} catch (NamingException ne) {
				ne.printStackTrace();
			} catch (JMSException jmse) {
				jmse.printStackTrace();
			}
			if (context != null && connection != null && receiver != null) {
				messagingInfoRecords.put(userId, new MessagingInfo(context, connection, receiver));
			}
		}
	}

	public void unsetMessageListener(Correspondent correspondent) {
		MessagingInfo messagingInfo = messagingInfoRecords.get(correspondent.getUserId());
		if (messagingInfo != null) {
			messagingInfo.close();
		}
	}

	private class MessagingInfo {
		private Context context;
		private Connection connection;
		private MessageConsumer receiver;

		public MessagingInfo(Context context, Connection connection, MessageConsumer receiver) {
			this.context = context;
			this.connection = connection;
			this.receiver = receiver;
		}

		public void close() {
			if (receiver != null) {
				try {
					receiver.setMessageListener(null);
				} catch (JMSException jmse1) {
					jmse1.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException jmse2) {
					jmse2.printStackTrace();
				}
			}
			if (context != null) {
				try {
					context.close();
				} catch (NamingException ne) {
					ne.printStackTrace();
				}
			}
		}
	}

	private class InMessageHandler implements MessageListener {
		String userId;
		MessageManager messageManager;

		public InMessageHandler(String userId, MessageManager messageManager) {
			this.userId = userId;
			this.messageManager = messageManager;
		}

		@Override
		public void onMessage(javax.jms.Message jmsmessage) {
			messageManager.addMessage(userId, translateMessage(jmsmessage));
		}

	}

}
