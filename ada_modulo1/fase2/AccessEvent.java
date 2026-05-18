package monitoring.stream;

public class AccessEvent {

    private String ip;
    private String endpoint;
    private long timestamp;
    private int responseTime;

    public AccessEvent(String ip,
                       String endpoint,
                       long timestamp,
                       int responseTime) {

        this.ip = ip;
        this.endpoint = endpoint;
        this.timestamp = timestamp;
        this.responseTime = responseTime;
    }

    public String getIp() {
        return ip;
    }

    public int getResponseTime() {
        return responseTime;
    }

    @Override
    public String toString() {
        return ip + " -> " + endpoint;
    }
}