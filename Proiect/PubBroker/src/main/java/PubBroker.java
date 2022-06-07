import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class PubBroker extends Thread {
    private final String EXCHANGE_NAME = "subs-exchange";
    private final String EXCHANGE_NAME_PUB = "direct_pubs";
    private final String RECV_PUB_QUEUE = "start-publications";
    private final Map<String, List<JSONArray>> routingTable = new HashMap<>();

    public void run() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            /* RECEIVING SUBSCRIPTIONS */
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, EXCHANGE_NAME, "");

            System.out.println(" [*] Waiting for forwarded subscriptions...");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String subscription = new String(delivery.getBody(), StandardCharsets.UTF_8);
                JSONObject subJson = new JSONObject(subscription);
                String guid = subJson.getString("id");

                if (!routingTable.containsKey(guid)) {
                    routingTable.put(guid, new ArrayList<>());
                }

                routingTable.get(guid).add(subJson.getJSONArray("subscription"));

                System.out.println(" [x] : '" + subscription + "'");
            };

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

        /* RECEIVING PUBLICATIONS */
        try {
            Connection recvPubConnection = factory.newConnection();
            Channel recvPubChannel = recvPubConnection.createChannel();
            recvPubChannel.queueDeclare(RECV_PUB_QUEUE, false, false, false, null);

            Connection sendNotifConnection = factory.newConnection();
            Channel sendNotifChannel = sendNotifConnection.createChannel();
            sendNotifChannel.exchangeDeclare(EXCHANGE_NAME_PUB, "direct");

            System.out.println(" [*] Waiting for publications...");

            DeliverCallback recvNotifCallback = (consumerTag, delivery) -> {
                String publication = new String(delivery.getBody(), StandardCharsets.UTF_8);
                JSONObject publicationJSON = new JSONObject(publication);

                System.out.println(" [x] Publication : '" + publicationJSON + "'");

                for (String key : routingTable.keySet()) {
                    List<JSONArray> currentSubscriptions = routingTable.get(key);

                    for (JSONArray s : currentSubscriptions) {
                        try {
                            if (match(publicationJSON, s)) {
                                System.out.println(" [x] Subscription : '" + s + "'");
                                System.out.println("A facut match!");
                                String response = s + "#" + publication;
                                sendNotifChannel.basicPublish(EXCHANGE_NAME_PUB, key, null, response.getBytes());
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            recvPubChannel.basicConsume(RECV_PUB_QUEUE, true, recvNotifCallback, consumerTag -> {
            });
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static boolean match(JSONObject publication, JSONArray subscription) throws ParseException {
        for(int i = 0; i < subscription.length(); i++) {
            JSONObject s = subscription.getJSONObject(i);

            switch (s.getString("field")) {
                case "company":
                    if(s.getString("operator").equals("=")) {
                        if(! s.getString("value").equals(publication.getString("company"))) {
                            return false;
                        }
                    }
                    else {
                        if(s.getString("value").equals(publication.getString("company"))) {
                            return false;
                        }
                    }
                    break;
                case "date":
                    SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date d1 = sdFormat.parse(s.getString("value"));
                    Date d2 = sdFormat.parse(publication.getString("date"));
                    switch (s.getString("operator")) {
                        case "<=":
                            if(d1.compareTo(d2) > 0)
                                return false;
                            break;
                        case ">=":
                            if(d1.compareTo(d2) < 0)
                                return false;
                            break;
                        case "=":
                            if(d1.compareTo(d2) != 0)
                                return false;
                            break;
                        case "!=":
                            if(d1.compareTo(d2) == 0)
                                return false;
                            break;
                    }
                    break;
                case "value":
                case "drop":
                case "variation":
                    Double number1 = Double.parseDouble(s.getString("value"));
                    Double number2 = Double.parseDouble(publication.getString(s.getString("field")));
                    switch (s.getString("operator")) {
                        case "<=":
                            if (number1 < number2)
                                return false;
                            break;
                        case "<":
                            if (number1 <= number2)
                                return false;
                            break;
                        case ">=":
                            if (number1 > number2)
                                return false;
                            break;
                        case ">":
                            if (number1 >= number2)
                                return false;
                            break;
                        case "=":
                            if (!number1.equals(number2))
                                return false;
                            break;
                        case "!=":
                            if (number1.equals(number2))
                                return false;
                            break;
                    }
            }
        }

        return true;
    }
}
