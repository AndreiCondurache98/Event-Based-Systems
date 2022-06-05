import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class PubBroker {
    private final static String EXCHANGE_NAME = "subs-exchange";
    private final static String SEND_NOTIF_QUEUE = "broker-forward-notification";
    private final static String RECV_PUB_QUEUE = "start-publications";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");
        System.out.println(" [*] Waiting for forwarded subscriptions...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String subscription = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] : '" + subscription + "'");
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });

        Connection recvNotifConnection = factory.newConnection();
        Channel recvNotifChannel = recvNotifConnection.createChannel();
        recvNotifChannel.queueDeclare(RECV_PUB_QUEUE, false, false, false, null);

        Connection sendNotifConnection = factory.newConnection();
        Channel sendNotifChannel = sendNotifConnection.createChannel();
        sendNotifChannel.queueDeclare(SEND_NOTIF_QUEUE, false, false, false, null);
        System.out.println(" [*] Waiting for publications...");

        DeliverCallback recvNotifCallback = (consumerTag, delivery) -> {
            String publication = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Publication : '" + publication + "'");

            sendNotifChannel.basicPublish("", SEND_NOTIF_QUEUE, null, publication.getBytes());
        };

        recvNotifChannel.basicConsume(RECV_PUB_QUEUE, true, recvNotifCallback, consumerTag -> { });
    }
}
