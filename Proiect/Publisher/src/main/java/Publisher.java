import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.json.JSONObject;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Publisher {
    private final static String QUEUE_NAME = "start-publications";
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");
    private static int sendPublications;

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        File myObj = new File("publications3.txt");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            Scanner myReader = new Scanner(myObj);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                JSONObject jsonObject = new JSONObject();

                Pattern regex = Pattern.compile("\\((.*?)\\)");
                Matcher regexMatcher = regex.matcher(data);

                while (regexMatcher.find()) {
                    String pub = regexMatcher.group(1);
                    String[] fields = pub.split(",");

                    jsonObject.put(fields[0], fields[1]);
                }

                LocalDateTime timeOfIssue = LocalDateTime.now();
                jsonObject.put("timeOfIssue", dtf.format(timeOfIssue));

                channel.basicPublish("", QUEUE_NAME, null, jsonObject.toString().getBytes());
                System.out.println(" [x] Sent '" + jsonObject + "'");
            }
            String data = myReader.nextLine();
            JSONObject jsonObject = new JSONObject();

            Pattern regex = Pattern.compile("\\((.*?)\\)");
            Matcher regexMatcher = regex.matcher(data);

            while (regexMatcher.find()) {
                String pub = regexMatcher.group(1);
                String[] fields = pub.split(",");

                jsonObject.put(fields[0], fields[1]);
            }

            LocalDateTime timeOfIssue = LocalDateTime.now();
            jsonObject.put("timeOfIssue", dtf.format(timeOfIssue));

            channel.basicPublish("", QUEUE_NAME, null, jsonObject.toString().getBytes());
            sendPublications += 1;
            System.out.println(" [x] Sent '" + jsonObject + "'");
        }
    }
}
