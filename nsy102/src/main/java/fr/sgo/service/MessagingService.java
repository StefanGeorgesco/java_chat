package fr.sgo.service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.Topic;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.TopicSession;
import javax.jms.JMSException;

import org.exolab.jms.message.MapMessageImpl;

import fr.sgo.app.App;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.InMessage;
import fr.sgo.entity.OutMessage;
import fr.sgo.model.MessageManager;
import fr.sgo.view.InformationMessage;

public class MessagingService {
	private static MessagingService instance = null;
	private Context context;
	private TopicConnectionFactory factory;
	private TopicConnection connection;
	private static final String factoryName = "JmsTopicConnectionFactory";
	private static final String topicName = "topic1";
	private TopicSession session;
	private TopicPublisher sender;
	private Map<String, Context> contextRecords; // key: url
	private Map<String, TopicConnectionFactory> factoryRecords; // key: url
	private Map<String, TopicConnection> connectionRecords; // key: url
	private Map<String, TopicSession> sessionRecords; // key: url
	private Map<String, MessageConsumer> receivers; // key: userId
	private App app;

	private MessagingService(App app) {
		this.app = app;
		this.contextRecords = Collections.synchronizedMap(new HashMap<String, Context>());
		this.factoryRecords = Collections.synchronizedMap(new HashMap<String, TopicConnectionFactory>());
		this.connectionRecords = Collections.synchronizedMap(new HashMap<String, TopicConnection>());
		this.sessionRecords = Collections.synchronizedMap(new HashMap<String, TopicSession>());
		this.receivers = Collections.synchronizedMap(new HashMap<String, MessageConsumer>());
	}

	public static MessagingService getInstance(App app) {
		if (instance == null)
			instance = new MessagingService(app);
		return instance;
	}

	public String getDestinationName() {
		return topicName;
	}

	public void open() {
		ProfileInfo profileInfo = app.getProfileInfo();
		try {
			Hashtable<String, String> props = new Hashtable<String, String>();
			props.put(Context.PROVIDER_URL,
					"rmi://" + profileInfo.getHost() + ":" + Integer.toString(profileInfo.getJMSPort()) + "/");
			props.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
			props.put(Context.SECURITY_PRINCIPAL, "admin");
			props.put(Context.SECURITY_CREDENTIALS, "openjms");
			context = new InitialContext(props);
			factory = (TopicConnectionFactory) context.lookup(factoryName);
			connection = factory.createTopicConnection();
			session = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			Topic topic = (Topic) context.lookup(topicName);
			sender = session.createPublisher(topic);
			connection.start();
			if (app.T)
				System.out.println("service jms local installé");
		} catch (NamingException ne) {
			ne.printStackTrace();
			System.exit(1);
		} catch (JMSException jmse) {
			jmse.printStackTrace();
			System.exit(1);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (context != null)
					try {
						context.close();
					} catch (NamingException ne) {
						ne.printStackTrace();
					}
				if (connection != null)
					try {
						connection.close();
					} catch (JMSException jmse) {
						jmse.printStackTrace();
					}
				for (Context context : contextRecords.values()) {
					if (context != null)
						try {
							context.close();
						} catch (NamingException ne) {
							ne.printStackTrace();
						}
				}
				for (TopicConnection connection : connectionRecords.values()) {
					if (connection != null)
						try {
							connection.close();
						} catch (JMSException jmse) {
							jmse.printStackTrace();
						}
				}
			}
		});
	}

	public void sendMessage(Correspondent correspondent, OutMessage message) {
		javax.jms.Message jmsMessage = translateMessage(message);
		try {
			jmsMessage.setStringProperty("InId", correspondent.getPairingInfo().getOutId());
			sender.send(jmsMessage);
		} catch (JMSException e) {
			e.printStackTrace();
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
				MessageConsumer receiver = null;
				try {
					int port = service.getProfileInfo().getJMSPort();
					String url = "rmi://" + host + ":" + Integer.toString(port) + "/";
					Context context = getContext(url);
					TopicConnection connection = getConnection(url);
					TopicSession session = getSession(url);
					String topicName = service.getDestinationName(app.getMainController(),
							correspondent.getPairingInfo().getOutId());
					Topic topic = (Topic) context.lookup(topicName);
					connection.stop();
					receiver = session.createDurableSubscriber(topic,
							correspondent.getPairingInfo().getInId(),
							"InId = '" + correspondent.getPairingInfo().getInId() + "'", true);
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
				if (receiver != null) {
					receivers.put(userId, receiver);
				}
			}
		}
	}

	public void unsetInMessagingHandler(Correspondent correspondent) {
		String userId = correspondent.getUserId();
		MessageConsumer receiver = receivers.get(userId);
		if (receiver != null)
			try {
				receiver.setMessageListener(null);
				receiver.close();
				receivers.remove(userId);
			} catch (JMSException e) {
				e.printStackTrace();
			}
	}

	private Context getContext(String url) {
		Context context = contextRecords.get(url);
		if (context == null) {
			Hashtable<String, String> props = new Hashtable<String, String>();
			props.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
			props.put(Context.SECURITY_PRINCIPAL, "admin");
			props.put(Context.SECURITY_CREDENTIALS, "openjms");
			props.put(Context.PROVIDER_URL, url);
			try {
				context = new InitialContext(props);
				contextRecords.put(url, context);
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}
		return context;
	}

	private TopicConnectionFactory getFactory(String url) {
		TopicConnectionFactory factory = factoryRecords.get(url);
		if (factory == null) {
			Context context = getContext(url);
			if (context != null)
				try {
					factory = (TopicConnectionFactory) context.lookup(factoryName);
					factoryRecords.put(url, factory);
				} catch (NamingException e) {
					e.printStackTrace();
				}
		}
		return factory;
	}

	private TopicConnection getConnection(String url) {
		TopicConnection connection = connectionRecords.get(url);
		if (connection == null) {
			TopicConnectionFactory factory = getFactory(url);
			if (factory != null)
				try {
					connection = factory.createTopicConnection();
					connectionRecords.put(url, connection);
				} catch (JMSException e) {
					e.printStackTrace();
				}
		}
		return connection;
	}

	private TopicSession getSession(String url) {
		TopicSession session = sessionRecords.get(url);
		if (session == null) {
			TopicConnection connection = getConnection(url);
			if (connection != null)
				try {
					session = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
					sessionRecords.put(url, session);
				} catch (JMSException e) {
					e.printStackTrace();
				}
		}
		return session;
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
			if (app.T)
				System.out.println(
						"message reçu de " + correspondent.getUserName() + " : " + applicationMessage.getContents());
			messageManager.addMessage(correspondent.getUserId(), applicationMessage);
		}

	}

}
