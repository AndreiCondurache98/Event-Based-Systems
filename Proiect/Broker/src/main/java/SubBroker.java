import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;

public class SubBroker extends Thread {
    private final String RECV_SUB_QUEUE = "start-subscriptions";
    private final String EXCHANGE_NAME = "subs-exchange";
    private final String EXCHANGE_NAME_PUB = "direct_pubs";
    private final String EXCHANGE_NAME_NOTIFY = "direct_notifications";
    private final String GUID = UUID.randomUUID().toString();

    /**
     * map with (publication, [sub's id]), where value set contains sub's id which receive notification for publication X.
     *  Scope: if subscriber has 2 subs which match one pub, he will be notified only ONCE
     */
    public static volatile ConcurrentMap<String, Set<String>> notificationSent = new ConcurrentHashMap<>();

    public void run() {
        /**
         * Routing table (subscription, [subscribers' guid]).
         *  Scope: PUB-BROKER respond with message "subscription#publication" and we need to know who the notification is for.
         *      PUB-BROKER tells what subscription matches current publication and according to routing table, we notify those subscribers who
         *          have sent current subscription.
         */
        Map<String, Set<String>> routingTable = new HashMap<>();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        /* RECEIVING SUBSCRIPTION AND FORWARD IT */
        try {
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

                JSONArray subArray = subAsJson.getJSONArray("subscription");
                String sub = subArray.toString();

                if (!routingTable.containsKey(sub)) {
                    routingTable.put(sub, new HashSet<>());
                }

                routingTable.get(sub).add(subAsJson.getString("id"));
                subAsJson.put("id", GUID);

                forwardSubChannel.basicPublish(EXCHANGE_NAME, "", null, subAsJson.toString().getBytes());
            };

            subChannel.basicConsume(RECV_SUB_QUEUE, true, deliverCallback, consumerTag -> { });
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

        /* RECEIVING PUBLICATION FROM PUB-BROKER AND NOTIFY SUBSCRIBER */

        try {
            Connection recvNotifConnection = factory.newConnection();
            Channel recvNotifChannel = recvNotifConnection.createChannel();
            recvNotifChannel.exchangeDeclare(EXCHANGE_NAME_PUB, "direct");
            String queueName = recvNotifChannel.queueDeclare().getQueue();
            recvNotifChannel.queueBind(queueName, EXCHANGE_NAME_PUB, GUID);

            System.out.println(" [*] Waiting for notifications...");

            Connection forwardNotifConnection = factory.newConnection();
            Channel forwardNotifChannel = forwardNotifConnection.createChannel();
            forwardNotifChannel.exchangeDeclare(EXCHANGE_NAME_NOTIFY, "direct");

            DeliverCallback notificationCallback = (consumerTag, delivery) -> {
                String notification = new String(delivery.getBody(), StandardCharsets.UTF_8);
                String[] response = notification.split("#");
                String subscriptionMatched = response[0];
                String publicationMatched = response[1];// + "#" + response[2];
                Set<String> guidsToNotify = routingTable.get(subscriptionMatched);

                for (String guid : guidsToNotify) {
                    String message = "[NOTIFICATION] Your subscription: " + subscriptionMatched + " matches publication: " + publicationMatched;

                    if (!notificationSent.containsKey(publicationMatched)) {
                        notificationSent.put(publicationMatched, new HashSet<>());
                        notificationSent.get(publicationMatched).add(guid);
                        forwardNotifChannel.basicPublish(EXCHANGE_NAME_NOTIFY, guid, null, message.getBytes());
                    } else if (!notificationSent.get(publicationMatched).contains(guid)) {
                        notificationSent.get(publicationMatched).add(guid);
                        forwardNotifChannel.basicPublish(EXCHANGE_NAME_NOTIFY, guid, null, message.getBytes());
                    }
                }
            };

            recvNotifChannel.basicConsume(queueName, true, notificationCallback, consumerTag -> { });
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
