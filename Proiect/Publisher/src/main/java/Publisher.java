import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Publisher {
    private final static String QUEUE_NAME = "start-publications";

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        File myObj = new File("publications.txt");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel();
             Scanner myReader = new Scanner(myObj)) {

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                List<String> matchList = new ArrayList<>();
                Pattern regex = Pattern.compile("\\((.*?)\\)");
                Matcher regexMatcher = regex.matcher(data);

                while (regexMatcher.find()) {
                    matchList.add(regexMatcher.group(1));
                    String pub = regexMatcher.group(1);
                    String[] fields = pub.split(",");

                    Map<String, String> map = new LinkedHashMap<>();
                    map.put("field", fields[0]);
                    map.put("value", fields[1]);

                    jsonArray.put(map);
                }
                jsonObject.put("publication", jsonArray);

                channel.basicPublish("", QUEUE_NAME, null, jsonObject.toString().getBytes());
                System.out.println(" [x] Sent '" + jsonObject + "'");
            }

        }
    }
}
