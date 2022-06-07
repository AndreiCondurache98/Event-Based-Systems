import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Broker {
    private final static String RECV_SUB_QUEUE = "start-subscriptions";
    private final static String EXCHANGE_NAME = "subs-exchange";

    private final static String RECV_NOTIFICATION_QUEUE = "broker-forward-notification";
    private final static String FORWARD_NOTIFICATION_QUEUE = "forward-notification";

    private Map<String, List<String>> subscriptions = new HashMap<>();

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection subConnection = factory.newConnection();
        Channel subChannel = subConnection.createChannel();
        subChannel.queueDeclare(RECV_SUB_QUEUE, false, false, false, null);

        Connection forwardSubConnection = factory.newConnection();
        Channel forwardSubChannel = forwardSubConnection.createChannel();
        forwardSubChannel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        System.out.println(" [*] Waiting for subscriptions...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String subscription = new String(delivery.getBody(), StandardCharsets.UTF_8);
            JSONObject subJson = new JSONObject(subscription);

            System.out.println(" [x] Received: '" + subscription + "'");

            forwardSubChannel.basicPublish(EXCHANGE_NAME, "", null, subscription.getBytes());
            System.out.println(" [v] Forwarded subscription to PubBroker.");
        };

        subChannel.basicConsume(RECV_SUB_QUEUE, true, deliverCallback, consumerTag -> { });

        Connection recvNotifConnection = factory.newConnection();
        Channel recvNotifChannel = recvNotifConnection.createChannel();
        recvNotifChannel.queueDeclare(RECV_NOTIFICATION_QUEUE, false, false, false, null);

        Connection forwardNotifConnection = factory.newConnection();
        Channel forwardNotifChannel = forwardNotifConnection.createChannel();
        forwardNotifChannel.queueDeclare(FORWARD_NOTIFICATION_QUEUE, false, false, false, null);

        System.out.println(" [*] Waiting for notifications...");

        DeliverCallback notificationCallback = (consumerTag, delivery) -> {
            String notification = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received: '" + notification + "'");

            forwardNotifChannel.basicPublish("", FORWARD_NOTIFICATION_QUEUE, null, notification.getBytes());
            System.out.println(" [v] Forwarded notification to Subscriber.");
        };

        recvNotifChannel.basicConsume(RECV_NOTIFICATION_QUEUE, true, notificationCallback, consumerTag -> { });
    }
}
