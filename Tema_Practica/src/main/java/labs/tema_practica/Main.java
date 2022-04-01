package labs.tema_practica;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrei
 */
public class Main {

    public static void main(String[] args) {
        Generator generator = new Generator();
        
        generator.generatePublications();
        generator.generateSubscriptions();
        
        writePubsIntoFile(generator.getPublications());        
        writeSubsIntoFile(generator.getSubscriptions());

    }

    public static void writePubsIntoFile(List<Publication> generatedPublications) {
        try {
            FileWriter writer = new FileWriter("publications.txt", false);
            for (Publication publication : generatedPublications) {
                writer.write(publication.toString());
            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void writeSubsIntoFile(List<Subscription> generatedSubscriptions) {
        try {
            FileWriter writer = new FileWriter("subscriptions.txt", false);
            for (Subscription subscription : generatedSubscriptions) {
                writer.write(subscription.toString());
            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
