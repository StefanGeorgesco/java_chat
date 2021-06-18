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
import fr.sgo.controller.RMIService;
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
	private Map<Chat, TopicConnection> inConnections;
	private Map<Chat, TopicConnection> outConnections;
	private Map<Chat, TopicPublisher> senders;
	private Map<Chat, TopicSubscriber> receivers;

	private MessagingService() {
		this.contexts = Collections.synchronizedMap(new HashMap<String, Context>());
		this.factories = Collections.synchronizedMap(new HashMap<String, TopicConnectionFactory>());
		this.inConnections = Collections.synchronizedMap(new HashMap<Chat, TopicConnection>());
		this.outConnections = Collections.synchronizedMap(new HashMap<Chat, TopicConnection>());
		this.senders = Collections.synchronizedMap(new HashMap<Chat, TopicPublisher>());
		this.receivers = Collections.synchronizedMap(new HashMap<Chat, TopicSubscriber>());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (TopicPublisher sender : senders.values()) {
					try {
						sender.close();
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
				for (TopicSubscriber receiver : receivers.values()) {
					try {
						receiver.close();
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
				for (Context context : contexts.values()) {
					if (context != null)
						try {
							context.close();
						} catch (NamingException ne) {
							ne.printStackTrace();
						}
				}
				for (TopicConnection connection : inConnections.values()) {
					if (connection != null)
						try {
							connection.close();
						} catch (JMSException jmse) {
							jmse.printStackTrace();
						}
				}
				for (TopicConnection connection : outConnections.values()) {
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

	public boolean sendMessage(Chat chat, OutMessage message) {
		boolean sent = false;
		javax.jms.Message jmsMessage = translateMessage(message);
		MessageProducer sender = senders.get(chat);
		if (sender != null) {
			try {
				jmsMessage.setStringProperty("InId", chat.getId());
				sender.send(jmsMessage);
				sent = true;
				if (App.T)
					System.out.println("message '" + message.getContents() + "' envoyé");
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		return sent;
	}

	private JMSInfo getJMSInfo(String host, int port, String topicName) {
		JMSInfo info = null;
		String url = "rmi://" + host + ":" + Integer.toString(port) + "/";
		Context context = getContext(url);
		TopicConnectionFactory factory = getFactory(url);
		Topic topic;
		try {
			topic = (Topic) context.lookup(topicName);
			info = new JMSInfo(factory, topic);
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
		unsetInMessagingHandler(chat);
		JMSInfo receiverJmsInfo = null;
		String InId = null;
		TopicConnection inConnection = null;
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
			inConnection = receiverJmsInfo.getFactory().createTopicConnection();
			inConnection.setClientID(chat.getSubscriberName());
			TopicSession session = inConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			inConnection.stop();
			if (App.JMS_MESSAGE_PERSISTENCE)
				receiver = session.createDurableSubscriber(receiverJmsInfo.getTopic(), chat.getSubscriberName(),
						"InId = '" + InId + "'", true);
			else
				receiver = session.createSubscriber(receiverJmsInfo.getTopic(),
						"InId = '" + InId + "'", true);
			receiver.setMessageListener(new InMessageHandler(chat));
			inConnections.put(chat, inConnection);
			receivers.put(chat, receiver);
			inConnection.start();
			if (App.T)
				System.out.println("récepteur installé pour le chat id=" + chat.getId() + ", subscriber name="
						+ chat.getSubscriberName() + " : " + receiver.toString());
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

	public void setOutMessagingHandler(Chat chat) {
		unsetOutMessagingHandler(chat);
		JMSInfo senderJmsInfo = null;
		TopicConnection outConnection = null;
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
			outConnection = senderJmsInfo.getFactory().createTopicConnection();
			TopicSession session = outConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			sender = session.createPublisher(senderJmsInfo.getTopic());
			outConnections.put(chat, outConnection);
			senders.put(chat, sender);
			if (App.T)
				System.out.println("émetteur installé pour le chat, id=" + chat.getId() + ", subscriber name="
						+ chat.getSubscriberName() + " : " + sender.toString());
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

	public void unsetInMessagingHandler(Chat chat) {
		TopicSubscriber receiver = receivers.get(chat);
		if (receiver != null)
			try {
				receiver.setMessageListener(null);
				receiver.close();
				if (App.T)
					System.out.println("récepteur désactivé pour le chat id=" + chat.getId() + ", subscriber name="
							+ chat.getSubscriberName() + " : " + receiver.toString());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		receivers.remove(chat);
		TopicConnection inConnection = inConnections.get(chat);
		if (inConnection != null)
			try {
				inConnection.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		inConnections.remove(chat);
	}

	public void unsetOutMessagingHandler(Chat chat) {
		MessageProducer sender = senders.get(chat);
		if (sender != null)
			try {
				sender.close();
				if (App.T)
					System.out.println("émetteur désactivé pour le chat, id=" + chat.getId() + ", subscriber name="
							+ chat.getSubscriberName() + " : " + sender.toString());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		senders.remove(chat);
		TopicConnection outConnection = inConnections.get(chat);
		if (outConnection != null)
			try {
				outConnection.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		inConnections.remove(chat);
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
			final javax.jms.Message m = jmsmessage;
			new Thread() {
				@Override
				public void run() {
					InMessage applicationMessage = translateMessage(m);
					Correspondent correspondent = applicationMessage.getAuthor();
					if (App.T)
						System.out.println("message reçu de " + correspondent.getUserName() + " : "
								+ applicationMessage.getContents());
					chat.addMessage(applicationMessage);
				}
			}.start();
		}

	}

	private class JMSInfo {
		private TopicConnectionFactory factory;
		private Topic topic;

		JMSInfo(TopicConnectionFactory factory, Topic topic) {
			this.factory = factory;
			this.topic = topic;
		}

		public TopicConnectionFactory getFactory() {
			return factory;
		}

		public Topic getTopic() {
			return topic;
		}

	}

}
