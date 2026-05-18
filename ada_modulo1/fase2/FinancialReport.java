package batch.reports;

public class FinancialReport {

    private String eventName;
    private double totalRevenue;
    private int totalTicketsSold;

    public FinancialReport(String eventName,
                           double totalRevenue,
                           int totalTicketsSold) {

        this.eventName = eventName;
        this.totalRevenue = totalRevenue;
        this.totalTicketsSold = totalTicketsSold;
    }

    @Override
    public String toString() {

        return "FinancialReport{" +
                "eventName='" + eventName + '\'' +
                ", totalRevenue=" + totalRevenue +
                ", totalTicketsSold=" + totalTicketsSold +
                '}';
    }
}