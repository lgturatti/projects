package catalog.domain;

import java.time.LocalDateTime;

public class Event {

    private Long id;
    private String title;
    private String description;
    private double price;
    private LocalDateTime dateTime;
    private int availableTickets;

    public Event(Long id,
                 String title,
                 String description,
                 double price,
                 LocalDateTime dateTime,
                 int availableTickets) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.dateTime = dateTime;
        this.availableTickets = availableTickets;
    }

    public boolean reserveTickets(int quantity) {

        if (availableTickets >= quantity) {
            availableTickets -= quantity;
            return true;
        }

        return false;
    }

    public void releaseTickets(int quantity) {
        availableTickets += quantity;
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    public String getTitle() {
        return title;
    }
}