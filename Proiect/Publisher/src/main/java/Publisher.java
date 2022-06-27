import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.File;
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
        File myObj = new File("publications.txt");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            Scanner myReader = new Scanner(myObj);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                Pattern regex = Pattern.compile("\\((.*?)\\)");
                Matcher regexMatcher = regex.matcher(data);

                List<String> fieldList = new ArrayList<>();

                while (regexMatcher.find()) {
                    String pub = regexMatcher.group(1);
                    String[] fields = pub.split(",");

                    fieldList.add(fields[1]);
                }

                Pub.Publication pub = Pub.Publication.newBuilder()
                        .setCompany(fieldList.get(0))
                        .setValue(Double.parseDouble(fieldList.get(1)))
                        .setDrop(Double.parseDouble(fieldList.get(2)))
                        .setVariation(Double.parseDouble(fieldList.get(3)))
                        .setDate(fieldList.get(4))
                        .build();

                channel.basicPublish("", QUEUE_NAME, null, pub.toByteArray());
                System.out.println(" [x] Sent '" + pub + "'");
            }
            /*String data = myReader.nextLine();
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
            System.out.println(" [x] Sent '" + jsonObject + "'");*/
        }
    }
}
