import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rabbitmq.client.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.Duration;

public class Subscriber extends Thread {
    private final String QUEUE_RECV = "recv-notifcations";
    private final String QUEUE_SEND = "start-subscriptions";
    private final String EXCHANGE_NAME_NOTIFY = "direct_notifications";
    private final String GUID = UUID.randomUUID().toString();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");
    private int receivedPublications = 0;

    public void run() {

        System.out.println("SUBSCRIBER GUID: " + GUID);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        File myObj = new File("subscriptions2.txt");

        /* WAITING FOR NOTIFICATIONS */
        Connection recvNotifConnection;
        Channel recvNotifChannel;
        try {
            recvNotifConnection = factory.newConnection();
            recvNotifChannel = recvNotifConnection.createChannel();
            recvNotifChannel.exchangeDeclare(EXCHANGE_NAME_NOTIFY, "direct");
            

            String queueName = recvNotifChannel.queueDeclare().getQueue();
            recvNotifChannel.queueBind(queueName, EXCHANGE_NAME_NOTIFY, GUID);

            System.out.println(" [*] Waiting for notifications...");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String notification = new String(delivery.getBody(), StandardCharsets.UTF_8);

                System.out.println(notification.split("#")[0]);

                /*String pubTimeOfIssue = notification.split("#")[1];
                LocalDateTime emitted = LocalDateTime.parse(pubTimeOfIssue, formatter);
                LocalDateTime currentTime = LocalDateTime.now();
                Duration duration = Duration.between(emitted, currentTime);

                FileWriter writer = new FileWriter("results.txt", true);
                writer.write(String.valueOf(duration.toMillis()) + "\n");
                writer.flush();
                writer.close();

                receivedPublications += 1;
                FileWriter writer1 = new FileWriter(GUID.toString()+".txt", false);
                writer1.write(String.valueOf(receivedPublications));
                writer1.flush();
                writer1.close();*/
            };

            recvNotifChannel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

        /* SENDING SUBSCRIPTIONS */
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel();
             Scanner myReader = new Scanner(myObj)) {

            channel.queueDeclare(QUEUE_SEND, false, false, false, null);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                JSONObject json = new JSONObject();
                JSONArray ja = new JSONArray();

                Pattern regex = Pattern.compile("\\((.*?)\\)");
                Matcher regexMatcher = regex.matcher(data);

                while (regexMatcher.find()) {
                    String sub = regexMatcher.group(1);
                    String[] fields = sub.split(",");

                    Map<String, String> m = new LinkedHashMap<>(2);
                    m.put("field", fields[0]);
                    m.put("operator", fields[1]);
                    m.put("value", fields[2]);

                    ja.put(m);
                }

                json.put("id", GUID);
                json.put("subscription", ja);

                channel.basicPublish("", QUEUE_SEND, null, json.toString().getBytes());
                System.out.println(" [x] Sent '" + json + "'");
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
