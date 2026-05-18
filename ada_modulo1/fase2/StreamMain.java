package monitoring.stream;

public class StreamMain {

    public static void main(String[] args) {

        RealTimeMonitor monitor =
                new RealTimeMonitor();

        for (int i = 0; i < 10; i++) {

            AccessEvent event =
                    new AccessEvent(
                            "192.168.0.1",
                            "/checkout",
                            System.currentTimeMillis(),
                            1200
                    );

            monitor.processEvent(event);
        }
    }
}