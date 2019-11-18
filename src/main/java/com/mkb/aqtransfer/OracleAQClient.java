package com.mkb.aqtransfer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import javax.jms.*;

import oracle.jms.AQjmsFactory;
import oracle.jms.AQjmsSession;


public class OracleAQClient {

    private static final int RECEIVE_TIMEOUT_MS = 6000;

    private static Logger logger = LogManager.getLogger(OracleAQClient.class);

    public static void main(String[] args) {
        try {
            ClientConfig config = ClientConfig.getConfig();
            if (args.length > 1 && args[1].equals("--all")) {
                transferAll(config);
            } else
                transferMessage(config);
        } catch (Exception e) {
            logger.error("Error occurred: " + e.getMessage(), e);
        }
    }

    private static void transferAll(ClientConfig config) throws JMSException {
        while (transferMessage(config)) {}
    }

    private static boolean transferMessage(ClientConfig config) throws JMSException {
        TextMessage msg;

        try (QueueConnection fromConn = getConnection(config.getFromHostName(), config.getFromSID(),
                config.getFromPort(), config.getFromUser(), config.getFromPassword(), "thin"))
        {
            msg = consumeMessage(fromConn, config.getFromQueueOwner(), config.getFromQueueName());
            if (msg == null) return false;
        }

        try (QueueConnection toConn = getConnection(config.getToHostName(), config.getToSID(),
                config.getToPort(), config.getToUser(), config.getToPassword(), "thin"))
        {
            sendMessage(toConn, config.getToQueueOwner(), config.getToQueueName(), msg);
        }

        return true;
    }


    private static QueueConnection getConnection(String hostname, String oracleSid, int port,
                                                String userName, String password, String driver) throws JMSException {
        // get connection factory, it should be got from LDAP, but I don't have one now
        QueueConnectionFactory factory = AQjmsFactory.getQueueConnectionFactory(hostname, oracleSid, port, driver);
        // create connection
        return factory.createQueueConnection(userName, password);
    }

    private static void sendMessage(QueueConnection conn, String user, String queueName, TextMessage message)
            throws JMSException {
        Session session = conn.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
        conn.start();
        logger.trace("Sending session started");

        // queue should be got from LDAP, but I don't have one now
        Queue queue = ((AQjmsSession) session).getQueue(user, queueName);
        MessageProducer producer = session.createProducer(queue);
//        TextMessage msg = session.createTextMessage(message);

        producer.send(message);
        logger.info("Sent message: " + message.getText());

        session.close();
        producer.close();
        logger.info("Sending session closed");
    }

    private static TextMessage consumeMessage(QueueConnection conn, String user, String queueName) throws JMSException {
        Session session = conn.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
        conn.start();
        logger.trace("Receiving session started");

        // queue should be got from LDAP, but I don't have one now
        Queue queue = ((AQjmsSession) session).getQueue(user, queueName);
        MessageConsumer consumer = session.createConsumer(queue);
        TextMessage msg = (TextMessage) consumer.receive(RECEIVE_TIMEOUT_MS);
        if (msg == null)
            logger.info("No message received: wait for message timeout");
        else
            logger.info("Message received: " + msg.getText());

        consumer.close();
        session.close();
        logger.trace("Receiving session closed");

        return msg;
    }
}