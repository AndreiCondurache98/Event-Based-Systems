public class Main {
    public static void main(String[] args) {
        Thread subscriber = new Subscriber();

        subscriber.start();
    }
}
