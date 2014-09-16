package it.cnr.cool.service;

import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

public class JBossJMSService extends JMSService {
	
	@Override
	Context getContext(Context context) {
		return context;
	}

	@Override
	QueueConnectionFactory getQueueConnectionFactory(Context context) throws NamingException {
		return  (QueueConnectionFactory) context.lookup(connectionFactoryName);
	}
	

}
