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
    }
}
