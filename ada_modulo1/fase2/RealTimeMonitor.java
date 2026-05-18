package monitoring.stream;

import java.util.*;

public class RealTimeMonitor {

    private final Map<String, Integer> requestCounter =
            new HashMap<>();

    public void processEvent(AccessEvent event) {

        requestCounter.put(
                event.getIp(),
                requestCounter.getOrDefault(
                        event.getIp(), 0
                ) + 1
        );

        int totalRequests =
                requestCounter.get(event.getIp());

        System.out.println(
                "Processing access from IP: "
                        + event.getIp()
        );

        if (totalRequests > 5) {

            System.out.println(
                    "ALERT: Possible bot detected for IP "
                            + event.getIp()
            );
        }

        if (event.getResponseTime() > 1000) {

            System.out.println(
                    "ALERT: High latency detected"
            );
        }
    }
}