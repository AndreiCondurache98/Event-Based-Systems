import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;

public class PubBroker {
    private final static String EXCHANGE_NAME = "subs-exchange";
    private final static String EXCHANGE_NAME_PUB = "direct_pubs";
    private final static String SEND_NOTIF_QUEUE = "broker-forward-notification";
    private final static String RECV_PUB_QUEUE = "start-publications";

    public static void main(String[] argv) throws Exception {

        HashMap<String, List<Object>> routingTable = new HashMap<>();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        /** RECEIVEING SUBSCRIPTIONS */
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for forwarded subscriptions...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String subscription = new String(delivery.getBody(), StandardCharsets.UTF_8);
            JSONObject subAsJson = new JSONObject(subscription);
            String sourceUUID = subAsJson.get("guid").toString();

            System.out.println(" [x] Received: '" + subscription + "'");

            if (!routingTable.containsKey(sourceUUID)) {
                List<Object> subscriptions = new ArrayList<>();
                subscriptions.add(subAsJson.get("subscription"));
                routingTable.put(sourceUUID, subscriptions);
            } else
            {
                routingTable.get(sourceUUID).add(subAsJson.get("subscription"));
            }
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });


        /**RECEIVING PUBLICATIONS*/
        Connection recvPubConnection = factory.newConnection();
        Channel recvPubChannel = recvPubConnection.createChannel();
        recvPubChannel.queueDeclare(RECV_PUB_QUEUE, false, false, false, null);

        Connection sendNotifConnection = factory.newConnection();
        Channel sendNotifChannel = sendNotifConnection.createChannel();
        sendNotifChannel.exchangeDeclare(EXCHANGE_NAME_PUB, "direct");

        System.out.println(" [*] Waiting for publications...");

        DeliverCallback recvNotifCallback = (consumerTag, delivery) -> {
            String publication = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Publication : '" + publication + "'");

            String receiverUUID = "NU STIM INCA"; //Aflat in urma operatiei de matching
            //String receiverUUID = "333";

            sendNotifChannel.basicPublish(EXCHANGE_NAME_PUB, receiverUUID, null, publication.getBytes());
        };

        recvPubChannel.basicConsume(RECV_PUB_QUEUE, true, recvNotifCallback, consumerTag -> { });
    }
}
