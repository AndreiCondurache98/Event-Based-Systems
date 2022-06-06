import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import org.json.JSONArray;
import org.json.JSONObject;

public class Subscriber {
    private final static String QUEUE_SEND = "start-subscriptions";
    //private final static String QUEUE_RECEIVE = "forward-notification";
    private final static String EXCHANGE_NAME_NOTIFY = "direct_notifications";

    public static void main(String[] args) throws IOException, TimeoutException {

        String uuid = UUID.randomUUID().toString();
        System.out.println("GENERATED GUID: " + uuid);
        //String uuid = "111";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        File myObj = new File("subscriptions.txt");

        /** WAITING FOR NOTICATIONS*/
        Connection recvNotifConnection = factory.newConnection();
        Channel recvNotifChannel = recvNotifConnection.createChannel();

        recvNotifChannel.exchangeDeclare(EXCHANGE_NAME_NOTIFY, "direct");
        String queueName = recvNotifChannel.queueDeclare().getQueue();
        recvNotifChannel.queueBind(queueName, EXCHANGE_NAME_NOTIFY, uuid);

        System.out.println(" [*] Waiting for notifications...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Notification: '" + message + "'");
        };

        recvNotifChannel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });

        /** SENDIND SUBSCRIPTIONS*/
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel();
             Scanner myReader = new Scanner(myObj)) {

            channel.queueDeclare(QUEUE_SEND, false, false, false, null);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                JSONObject json = new JSONObject();
                JSONArray ja = new JSONArray();

                List<String> matchList = new ArrayList<>();
                Pattern regex = Pattern.compile("\\((.*?)\\)");
                Matcher regexMatcher = regex.matcher(data);

                while (regexMatcher.find()) {
                    matchList.add(regexMatcher.group(1));
                    String sub = regexMatcher.group(1);
                    String[] fields = sub.split(",");

                    Map<String, String> m = new LinkedHashMap<>(2);
                    m.put("field", fields[0]);
                    m.put("operator", fields[1]);
                    m.put("value", fields[2]);

                    ja.put(m);
                }

                json.put("subscription", ja);
                json.put("guid", uuid);

                channel.basicPublish("", QUEUE_SEND, null, json.toString().getBytes());
                System.out.println(" [x] Sent '" + json + "'");
            }

        }
    }
}
