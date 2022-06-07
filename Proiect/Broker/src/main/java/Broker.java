import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.UUID;

public class Broker {
    private final static String RECV_SUB_QUEUE = "start-subscriptions";
    private final static String EXCHANGE_NAME = "subs-exchange";
    private final static String EXCHANGE_NAME_PUB = "direct_pubs";
    private final static String EXCHANGE_NAME_NOTIFY = "direct_notifications";
    //private final static String EXCHANGE_NAME = "forward-notification";

    private final static String RECV_NOTIFICATION_QUEUE = "broker-forward-notification";
    private final static String FORWARD_NOTIFICATION_QUEUE = "forward-notification";

    private Map<String, List<String>> subscriptions = new HashMap<>();

    public static void main(String[] argv) throws Exception {

        HashMap<String, List<Object>> routingTable = new HashMap<>();

        String uuid = UUID.randomUUID().toString();
        System.out.println("GENERATED GUID: " + uuid);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        /** RECEIVEING SUBSCRIPTION AND FORWARD IT*/
        Connection subConnection = factory.newConnection();
        Channel subChannel = subConnection.createChannel();
        subChannel.queueDeclare(RECV_SUB_QUEUE, false, false, false, null);

        Connection forwardSubConnection = factory.newConnection();
        Channel forwardSubChannel = forwardSubConnection.createChannel();
        forwardSubChannel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        System.out.println(" [*] Waiting for subscriptions...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String subscription = new String(delivery.getBody(), StandardCharsets.UTF_8);
            JSONObject subAsJson = new JSONObject(subscription);
            System.out.println(" [x] Received: '" + subscription + "'");

            subAsJson.put("guid", uuid);

            if (!routingTable.containsKey(uuid)) {
                List<Object> subscriptions = new ArrayList<>();
                subscriptions.add(subAsJson.get("subscription"));
                routingTable.put(uuid, subscriptions);
            } else
            {
                routingTable.get(uuid).add(subAsJson.get("subscription"));
            }

            forwardSubChannel.basicPublish(EXCHANGE_NAME, "", null, subAsJson.toString().getBytes());
            System.out.println(" [v] Forwarded subscription to PubBroker.");
        };

        subChannel.basicConsume(RECV_SUB_QUEUE, true, deliverCallback, consumerTag -> { });

        /** RECEIVEING PUBLICATION FROM PUBBROKER AND NOTIFY SUBSCRIBER */

        Connection recvNotifConnection = factory.newConnection();
        Channel recvNotifChannel = recvNotifConnection.createChannel();
        recvNotifChannel.exchangeDeclare(EXCHANGE_NAME_PUB, "direct");
        String queueName = recvNotifChannel.queueDeclare().getQueue();
        recvNotifChannel.queueBind(queueName, EXCHANGE_NAME_PUB, uuid);

        System.out.println(" [*] Waiting for notifications...");

        Connection forwardNotifConnection = factory.newConnection();
        Channel forwardNotifChannel = forwardNotifConnection.createChannel();
        forwardNotifChannel.exchangeDeclare(EXCHANGE_NAME_NOTIFY, "direct");

        DeliverCallback notificationCallback = (consumerTag, delivery) -> {
            String notification = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received: '" + notification + "'");

            String receiverUUID = "NU STIM INCA"; //Aflat in urma operatiei de matching

            String message2 = "AI FOST NOTIFICAT";

            forwardNotifChannel.basicPublish(EXCHANGE_NAME_NOTIFY, receiverUUID, null, message2.getBytes());
            System.out.println(" [v] Forwarded notification to Subscriber.");
        };

        recvNotifChannel.basicConsume(queueName, true, notificationCallback, consumerTag -> { });

    }
}
