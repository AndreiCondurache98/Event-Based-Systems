import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class App {

    public static void main( String[] args ) throws FileNotFoundException {
        int averageLatency = 0, nrOfElements = 0;

        File file = new File("..\\Subscriber\\results.txt");
        Scanner myReader = new Scanner(file);
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            int value = Integer.parseInt(data);
            averageLatency += value;
            nrOfElements++;
        }
        System.out.println("AVERAGE LATENCY: " + (float)averageLatency/nrOfElements);
        myReader.close();

        int value1 = getNumberOfNotifForSubscriber("..\\Subscriber\\d76d4a07-c159-46ab-9130-bc2bdd679b9d.txt");
        int value2 = getNumberOfNotifForSubscriber("..\\Subscriber\\e28687b3-d885-4bb1-8e24-f88074d58413.txt");

        System.out.println("FIRST SUBSCRIBER RECEIVED: " + value1 + " notifications");
        System.out.println("FIRST SUBSCRIBER RECEIVED: " + value2 + " notifications");
    }

    private static int getNumberOfNotifForSubscriber(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        Scanner myReader = new Scanner(file);
        String nrOfNotif = myReader.nextLine();
        myReader.close();

        return Integer.parseInt(nrOfNotif);
    }
}
