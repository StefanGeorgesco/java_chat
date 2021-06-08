package fr.sgo.service;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.TopicSubscriber;
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
	private static final String factoryName = "JmsTopicConnectionFactory";
	private static final String topicName = "topic1";
	private Map<String, Context> contexts; // key=url
	private Map<String, TopicConnectionFactory> factories; // key=url
	private Map<String, TopicConnection> connections; // key=url
	private Map<String, TopicSession> sessions; // key=url
	private Map<Chat, TopicPublisher> senders;
	private Map<Chat, TopicSubscriber> receivers;

	private MessagingService() {
		this.contexts = Collections.synchronizedMap(new HashMap<String, Context>());
		this.factories = Collections.synchronizedMap(new HashMap<String, TopicConnectionFactory>());
		this.connections = Collections.synchronizedMap(new HashMap<String, TopicConnection>());
		this.sessions = Collections.synchronizedMap(new HashMap<String, TopicSession>());
		this.senders = Collections.synchronizedMap(new HashMap<Chat, TopicPublisher>());
		this.receivers = Collections.synchronizedMap(new HashMap<Chat, TopicSubscriber>());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (Context context : contexts.values()) {
					if (context != null)
						try {
							context.close();
						} catch (NamingException ne) {
							ne.printStackTrace();
						}
				}
				for (TopicConnection connection : connections.values()) {
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

	public static synchronized MessagingService getInstance() {
		if (instance == null)
			instance = new MessagingService();
		return instance;
	}

	public String getDestinationName() {
		return topicName;
	}

	public void sendMessage(Chat chat, OutMessage message) {
		javax.jms.Message jmsMessage = translateMessage(message);
		MessageProducer sender = senders.get(chat);
		try {
			jmsMessage.setStringProperty("InId", chat.getId());
			sender.send(jmsMessage);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private JMSInfo getJMSInfo(String host, int port, String topicName) {
		JMSInfo info = null;
		try {
			String url = "rmi://" + host + ":" + Integer.toString(port) + "/";
			Context context = getContext(url);
			TopicConnection connection = getConnection(url);
			TopicSession session = getSession(url);
			Topic topic = (Topic) context.lookup(topicName);
			info = new JMSInfo(session, topic, connection);
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return info;
	}
	
	private JMSInfo getJMSInfo() {
		ProfileInfo profileInfo = ProfileInfo.getInstance();
		String host = profileInfo.getHost();
		int port = profileInfo.getJMSPort();
		return getJMSInfo(host, port, topicName);
	}

	private JMSInfo getJMSInfo(Correspondent correspondent) {
		String host = null;
		int port = 0;
		String topicName = null;
		String userId = correspondent.getUserId();
		CorrespondentServiceInfo correspondentServiceInfo = CorrespondentServiceLocator.getInstance().lookup(userId);
		if (correspondentServiceInfo != null) {
			host = correspondentServiceInfo.getHost();
			RMIService service = correspondentServiceInfo.getServiceRMI();
			try {
				port = service.getProfileInfo().getJMSPort();
				topicName = service.getDestinationName(RMIController.getInstance(),
						correspondent.getPairingInfo().getOutId());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return getJMSInfo(host, port, topicName);
	}

	public void setInMessagingHandler(Chat chat) {
		JMSInfo receiverJmsInfo = null;
		String InId = null;
		TopicSubscriber receiver = null;
		if (chat instanceof HostedGroupChat) {
			receiverJmsInfo = getJMSInfo();
			InId = chat.getId();
		} else {
			Correspondent correspondent;
			if (chat instanceof CorrespondentChat) {
				correspondent = ((CorrespondentChat) chat).getCorrespondent();
				receiverJmsInfo = getJMSInfo(correspondent);
				InId = correspondent.getPairingInfo().getInId();
			} else if (chat instanceof RemoteGroupChat) {
				correspondent = ((RemoteGroupChat) chat).getCorrespondent();
				receiverJmsInfo = getJMSInfo(correspondent);
				InId = chat.getId();
			}
		}
		try {
			receiverJmsInfo.getConnection().stop();
			receiver = receiverJmsInfo.getSession().createDurableSubscriber(receiverJmsInfo.getTopic(),
					chat.getSubscriberName(), "InId = '" + InId + "'", true);
			receiver.setMessageListener(new InMessageHandler(chat));
			receiverJmsInfo.getConnection().start();
		} catch (JMSException e) {
			e.printStackTrace();
		}

		if (receiver != null)
			receivers.put(chat, receiver);
		else
			receivers.remove(chat);
	}

	public void setOutMessagingHandler(Chat chat) {
		JMSInfo senderJmsInfo = null;
		TopicPublisher sender = null;
		if (chat instanceof HostedGroupChat) {
			senderJmsInfo = getJMSInfo();
		} else {
			Correspondent correspondent;
			if (chat instanceof CorrespondentChat) {
				correspondent = ((CorrespondentChat) chat).getCorrespondent();
				senderJmsInfo = getJMSInfo();
			} else if (chat instanceof RemoteGroupChat) {
				correspondent = ((RemoteGroupChat) chat).getCorrespondent();
				senderJmsInfo = getJMSInfo(correspondent);
			}
		}
		try {
			sender = senderJmsInfo.getSession().createPublisher(senderJmsInfo.getTopic());
		} catch (JMSException e) {
			e.printStackTrace();
		}

		if (sender != null)
			senders.put(chat, sender);
		else
			senders.remove(chat);
	}

	public void unsetInMessagingHandler(Chat chat) {
		TopicSubscriber receiver = receivers.get(chat);
		if (receiver != null)
			try {
				receiver.setMessageListener(null);
				receiver.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		receivers.remove(chat);
	}

	public void unsetOutMessagingHandler(Chat chat) {
		MessageProducer sender = senders.get(chat);
		if (sender != null)
			try {
				sender.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		senders.remove(chat);
	}

	private Context getContext(String url) {
		Context context = contexts.get(url);
		if (context == null) {
			Hashtable<String, String> props = new Hashtable<String, String>();
			props.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
			props.put(Context.SECURITY_PRINCIPAL, "admin");
			props.put(Context.SECURITY_CREDENTIALS, "openjms");
			props.put(Context.PROVIDER_URL, url);
			try {
				context = new InitialContext(props);
				contexts.put(url, context);
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}
		return context;
	}

	private TopicConnectionFactory getFactory(String url) {
		TopicConnectionFactory factory = factories.get(url);
		if (factory == null) {
			Context context = getContext(url);
			if (context != null)
				try {
					factory = (TopicConnectionFactory) context.lookup(factoryName);
					factories.put(url, factory);
				} catch (NamingException e) {
					e.printStackTrace();
				}
		}
		return factory;
	}

	private TopicConnection getConnection(String url) {
		TopicConnection connection = connections.get(url);
		if (connection == null) {
			TopicConnectionFactory factory = getFactory(url);
			if (factory != null)
				try {
					connection = factory.createTopicConnection();
					connections.put(url, connection);
				} catch (JMSException e) {
					e.printStackTrace();
				}
		}
		return connection;
	}

	private TopicSession getSession(String url) {
		TopicSession session = sessions.get(url);
		if (session == null) {
			TopicConnection connection = getConnection(url);
			if (connection != null)
				try {
					session = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
					sessions.put(url, session);
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
