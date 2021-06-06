package fr.sgo.service;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.exolab.jms.message.MapMessageImpl;

import fr.sgo.app.App;
import fr.sgo.controller.RMIController;
import fr.sgo.entity.Chat;
import fr.sgo.entity.Correspondent;
import fr.sgo.entity.CorrespondentChat;
import fr.sgo.entity.HostedGroupChat;
import fr.sgo.entity.InMessage;
import fr.sgo.entity.OutMessage;
import fr.sgo.entity.RemoteGroupChat;
import fr.sgo.model.CorrespondentManager;
import fr.sgo.view.InformationView;

public class MessagingService {
	private static MessagingService instance = null;
	private Context context;
	private TopicConnectionFactory factory;
	private TopicConnection connection;
	private static final String factoryName = "JmsTopicConnectionFactory";
	private static final String topicName = "topic1";
	private TopicSession session;
	private TopicPublisher sender;
	private Map<String, Context> contextRecords; // key=url
	private Map<String, TopicConnectionFactory> factoryRecords; // key=url
	private Map<String, TopicConnection> connectionRecords; // key=url
	private Map<String, TopicSession> sessionRecords; // key=url
	private Map<Chat, MessageProducer> senders;
	private Map<Chat, MessageConsumer> receivers;

	private MessagingService() {
		this.contextRecords = Collections.synchronizedMap(new HashMap<String, Context>());
		this.factoryRecords = Collections.synchronizedMap(new HashMap<String, TopicConnectionFactory>());
		this.connectionRecords = Collections.synchronizedMap(new HashMap<String, TopicConnection>());
		this.sessionRecords = Collections.synchronizedMap(new HashMap<String, TopicSession>());
		this.senders = Collections.synchronizedMap(new HashMap<Chat, MessageProducer>());
		this.receivers = Collections.synchronizedMap(new HashMap<Chat, MessageConsumer>());
	}

	public static synchronized MessagingService getInstance() {
		if (instance == null)
			instance = new MessagingService();
		return instance;
	}

	public String getDestinationName() {
		return topicName;
	}

	public void open() {
		ProfileInfo profileInfo = ProfileInfo.getInstance();
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
			if (App.T)
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

	public void sendMessage(Chat chat, OutMessage message) {
		javax.jms.Message jmsMessage = translateMessage(message);
		try {
			jmsMessage.setStringProperty("InId", chat.getId());
			sender.send(jmsMessage);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private JMSInfo getJMSInfo(Correspondent correspondent) {
		JMSInfo info = null;
		String userId = correspondent.getUserId();
		CorrespondentServiceInfo correspondentServiceInfo = CorrespondentServiceLocator.getInstance().lookup(userId);
		if (correspondentServiceInfo != null) {
			String host = correspondentServiceInfo.getHost();
			RMIService service = correspondentServiceInfo.getServiceRMI();
			try {
				int port = service.getProfileInfo().getJMSPort();
				String url = "rmi://" + host + ":" + Integer.toString(port) + "/";
				Context context = getContext(url);
				TopicConnection connection = getConnection(url);
				TopicSession session = getSession(url);
				String topicName = service.getDestinationName(RMIController.getInstance(),
						correspondent.getPairingInfo().getOutId());
				Topic topic = (Topic) context.lookup(topicName);
				info = new JMSInfo(session, topic, connection);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}
		return info;
	}

	public void setMessagingHandlers(Chat chat) {
		MessageProducer sender = null;
		MessageConsumer receiver = null;
		if (chat instanceof HostedGroupChat) {
			sender = this.sender;
			try {
				Topic topic = (Topic) context.lookup(topicName);
				connection.stop();
				receiver = session.createDurableSubscriber(topic, chat.getSubscriberName(),
						"InId = '" + chat.getId() + "'", true);
				receiver.setMessageListener(new InMessageHandler(chat));
				connection.start();
			} catch (JMSException e) {
				e.printStackTrace();
			} catch (NamingException e) {
				e.printStackTrace();
			}
		} else {
			Correspondent correspondent;
			JMSInfo jmsInfo = null;
			String InId = null;
			if (chat instanceof CorrespondentChat) {
				correspondent = ((CorrespondentChat) chat).getCorrespondent();
				jmsInfo = getJMSInfo(correspondent);
				InId = correspondent.getPairingInfo().getInId();
				sender = this.sender;
			} else if (chat instanceof RemoteGroupChat) {
				if (App.T)
					System.out.println("ajout du RemoteGroupChat id = " + chat.getId());
				correspondent = ((RemoteGroupChat) chat).getCorrespondent();
				jmsInfo = getJMSInfo(correspondent);
				InId = chat.getId();
				try {
					sender = jmsInfo.getSession().createProducer(jmsInfo.getTopic());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
			try {
				jmsInfo.getConnection().stop();
				receiver = jmsInfo.getSession().createDurableSubscriber(jmsInfo.getTopic(), chat.getSubscriberName(),
						"InId = '" + InId + "'", true);
				receiver.setMessageListener(new InMessageHandler(chat));
				jmsInfo.getConnection().start();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		if (sender != null)
			senders.put(chat, sender);
		else
			senders.remove(chat);
		if (receiver != null)
			receivers.put(chat, receiver);
		else
			receivers.remove(chat);
	}

	public void unsetInMessagingHandlers(Chat chat) {
		MessageProducer sender = senders.get(chat);
		MessageConsumer receiver = receivers.get(chat);
		if (sender != null && !sender.equals(this.sender))
			try {
				sender.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		if (receiver != null)
			try {
				receiver.setMessageListener(null);
				receiver.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		senders.remove(chat);
		receivers.remove(chat);
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
		MapMessageImpl jmsMessage = null;
		try {
			jmsMessage = new MapMessageImpl();
			jmsMessage.setString("contents", applicationMessage.getContents());
			jmsMessage.setLong("timeWritten", applicationMessage.getTimeWritten());
			jmsMessage.setString("userId", applicationMessage.getUserId());
		} catch (JMSException e) {
			e.printStackTrace();
			new InformationView("Le message jms " + applicationMessage.getContents() + " n'a pas pu être créé");
		}
		return jmsMessage;
	}

	private InMessage translateMessage(javax.jms.Message jmsMessage) {
		InMessage applicationMessage = null;
		Correspondent correspondent = null;
		try {
			String userId = ((MapMessageImpl) jmsMessage).getString("userId");
			if (userId.equals(ProfileInfo.getInstance().getUserId()))
				correspondent = new Correspondent(userId, ProfileInfo.getInstance().getUserName());
			else
				correspondent = CorrespondentManager.getInstance().getCorrespondent(userId);
			applicationMessage = new InMessage(((MapMessageImpl) jmsMessage).getString("contents"),
					((MapMessageImpl) jmsMessage).getLong("timeWritten"), correspondent);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return applicationMessage;
	}

	private class InMessageHandler implements MessageListener {
		Chat chat;

		public InMessageHandler(Chat chat) {
			this.chat = chat;
		}

		@Override
		public void onMessage(javax.jms.Message jmsmessage) {
			InMessage applicationMessage = translateMessage(jmsmessage);
			Correspondent correspondent = applicationMessage.getAuthor();
			if (App.T)
				System.out.println(
						"message reçu de " + correspondent.getUserName() + " : " + applicationMessage.getContents());
			chat.addMessage(applicationMessage);
		}

	}

	private class JMSInfo {
		private TopicSession session;
		private Topic topic;
		private TopicConnection connection;

		JMSInfo(TopicSession session, Topic topic, TopicConnection connection) {
			this.session = session;
			this.topic = topic;
			this.connection = connection;
		}

		public TopicSession getSession() {
			return session;
		}

		public Topic getTopic() {
			return topic;
		}

		public TopicConnection getConnection() {
			return connection;
		}

	}

}
