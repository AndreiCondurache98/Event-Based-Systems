public class Main {
    public static void main(String[] args) {
        Thread pubBroker = new PubBroker();

        pubBroker.start();
    }
}
