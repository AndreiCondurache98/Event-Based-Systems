public class Main {
    public static void main(String[] args) {
        Thread subBroker = new SubBroker();
        Thread subBroker2 = new SubBroker();

        subBroker.start();
        subBroker2.start();
    }
}
