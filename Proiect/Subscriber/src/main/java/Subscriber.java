import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class Subscriber {
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        File myObj = new File("subscriptions.txt");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel();
             Scanner myReader = new Scanner(myObj)) {

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                channel.basicPublish("", QUEUE_NAME, null, data.getBytes());
                System.out.println(" [x] Sent '" + data + "'");
            }
        }
    }
}
