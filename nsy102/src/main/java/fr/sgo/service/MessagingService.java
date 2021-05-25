package fr.sgo.service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.JMSException;

import org.exolab.jms.message.MapMessageImpl;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.InMessage;
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
	private Map<String, OutMessagingInfo> outMessagingInfoRecords;
	private Map<String, InMessagingInfo> inMessagingInfoRecords;
	private Set<Integer> queueNumbers;
	private App app;

	private MessagingService(App app) {
		this.app = app;
		this.outMessagingInfoRecords = Collections.synchronizedMap(new HashMap<String, OutMessagingInfo>());
		this.inMessagingInfoRecords = Collections.synchronizedMap(new HashMap<String, InMessagingInfo>());
		this.queueNumbers = new HashSet<Integer>();
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
			Hashtable<String, String> props = new Hashtable<String, String>();
			props.put(Context.PROVIDER_URL, "rmi://" + app.getProfileInfo().getHost() + ":"
					+ Integer.toString(app.getProfileInfo().getJMSPort()) + "/");
			props.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
			props.put(Context.SECURITY_PRINCIPAL, "admin");
			props.put(Context.SECURITY_CREDENTIALS, "openjms");
			context = new InitialContext(props);
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
		for (Correspondent correspondent : app.getCorrespondentManager().getPairedCorrespondents()) {
			setOutMessagingHandler(correspondent);
		}
	}

	private void setOutMessagingHandler(Correspondent correspondent) {
		String userId = correspondent.getUserId();
		if (correspondent.isPaired()) {
			OutMessagingInfo outMessagingInfo = outMessagingInfoRecords.get(userId);
			if (outMessagingInfo == null) {
				int queueNumber;
				do {
					queueNumber = new Random().nextInt(10);
				} while (queueNumbers.contains(queueNumber));
				queueNumbers.add(queueNumber);
				String destinationName = "queue" + Integer.toString(queueNumber);
				Destination destination = null;
				MessageProducer sender = null;
				try {
					destination = (Destination) context.lookup(destinationName);
					if (T)
						System.out.println("file " + destinationName + " créée");
				} catch (NamingException e1) {
					e1.printStackTrace();
					if (T)
						System.out.println("La file pour " + correspondent.getUserName() + " n'a pas pu être créée");
				}
				try {
					sender = session.createProducer(destination);
					if (T)
						System.out.println("producteur de messages sur la file " + destinationName + " créé");
				} catch (JMSException e) {
					e.printStackTrace();
					if (T)
						System.out.println(
								"Le producteur de message sur la file " + destinationName + " n'a pas pu être créé");
				}
				if (sender != null)
					outMessagingInfoRecords.put(userId, new OutMessagingInfo(destination, sender));
			}
		}
	}

	public String getDestinationName(Correspondent correspondent) {
		String destinationName = "error";
		try {
			destinationName = ((Queue) outMessagingInfoRecords.get(correspondent.getUserId()).getDestination())
					.getQueueName();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return destinationName;
	}

	public void sendMessage(Correspondent correspondent, OutMessage message) {
		OutMessagingInfo outMessagingInfo = outMessagingInfoRecords.get(correspondent.getUserId());
		if (outMessagingInfo != null) {
			try {
				outMessagingInfo.getSender().send(translateMessage(message));
				if (T)
					System.out.println("message " + message.getContents() + " envoyé sur la file "
							+ ((Queue) outMessagingInfo.getDestination()).getQueueName());
			} catch (JMSException e) {
				e.printStackTrace();
				if (T)
					System.out.println("Le message jms " + message.getContents() + " n'a pas pu être envoyé à "
							+ correspondent.getUserName());
			}
		}
	}

	public void setInMessagingHandler(Correspondent correspondent) {
		if (correspondent.isPaired()) {
			String userId = correspondent.getUserId();
			CorrespondentServiceInfo correspondentServiceInfo = MessagingService.this.app
					.getCorrespondentServiceLocator().lookup(userId);
			if (correspondentServiceInfo != null) {
				String host = correspondentServiceInfo.getHost();
				ServiceRMI service = correspondentServiceInfo.getServiceRMI();
				Hashtable<String, String> props = new Hashtable<String, String>();
				props.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
				props.put(Context.SECURITY_PRINCIPAL, "admin");
				props.put(Context.SECURITY_CREDENTIALS, "openjms");
				int port;
				String destinationName;
				Context context = null;
				Connection connection = null;
				MessageConsumer receiver = null;
				try {
					port = service.getProfileInfo().getJMSPort();
					props.put(Context.PROVIDER_URL, "rmi://" + host + ":" + Integer.toString(port) + "/");
					context = new InitialContext(props);
					ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
					connection = factory.createConnection();
					Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
					destinationName = service.getDestinationName(app.getMainController(),
							correspondent.getPairingInfo().getOutId());
					if (T)
						System.out.println(
								"nom de la file pour " + correspondent.getUserName() + " : " + destinationName);
					Destination destination = (Destination) context.lookup(destinationName);
					receiver = session.createConsumer(destination);
					receiver.setMessageListener(
							new InMessageHandler(correspondent, MessagingService.this.app.getMessageManager()));
					connection.start();
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NamingException e) {
					e.printStackTrace();
				} catch (JMSException e) {
					e.printStackTrace();
				}

				if (context != null && connection != null && receiver != null) {
					inMessagingInfoRecords.put(userId, new InMessagingInfo(context, connection, receiver));
				}
			}
		}
	}

	public void unsetInMessagingHandler(Correspondent correspondent) {
		InMessagingInfo messagingInfo = inMessagingInfoRecords.get(correspondent.getUserId());
		if (messagingInfo != null) {
			messagingInfo.close();
		}
	}

	private javax.jms.Message translateMessage(OutMessage applicationMessage) {
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

	private InMessage translateMessage(javax.jms.Message jmsMessage) {
		InMessage applicationMessage = null;
		try {
			applicationMessage = new InMessage(jmsMessage.getStringProperty("contents"),
					jmsMessage.getLongProperty("timeWritten"));
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return applicationMessage;
	}

	private class OutMessagingInfo {
		private Destination destination;
		private MessageProducer sender;

		public OutMessagingInfo(Destination destination, MessageProducer sender) {
			this.destination = destination;
			this.sender = sender;
		}

		public Destination getDestination() {
			return destination;
		}

		public MessageProducer getSender() {
			return sender;
		}

	}

	private class InMessagingInfo {
		private Context context;
		private Connection connection;
		private MessageConsumer receiver;

		public InMessagingInfo(Context context, Connection connection, MessageConsumer receiver) {
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
		Correspondent correspondent;
		MessageManager messageManager;

		public InMessageHandler(Correspondent correspondent, MessageManager messageManager) {
			this.correspondent = correspondent;
			this.messageManager = messageManager;
		}

		@Override
		public void onMessage(javax.jms.Message jmsmessage) {
			InMessage applicationMessage = translateMessage(jmsmessage);
			applicationMessage.setCorrespondent(correspondent);
			if (T)
				System.out.println("message reçu de " + correspondent.getUserName() +
						" : " + applicationMessage.getContents());
			messageManager.addMessage(correspondent.getUserId(), applicationMessage);
		}

	}

}
