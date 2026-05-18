package shared.resilience;

public class CircuitBreaker {

    private int failureCount = 0;
    private boolean open = false;

    public boolean allowRequest() {
        return !open;
    }

    public void recordSuccess() {
        failureCount = 0;
        open = false;
    }

    public void recordFailure() {
        failureCount++;

        if (failureCount >= 3) {
            open = true;
        }
    }

    public boolean isOpen() {
        return open;
    }
}