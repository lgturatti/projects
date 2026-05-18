package batch.reports;

import java.util.List;

public class BatchMain {

    public static void main(String[] args) {

        BatchReportService service =
                new BatchReportService();

        List<FinancialReport> reports =
                service.generateDailyReports();

        System.out.println(
                "Generating financial reports..."
        );

        reports.forEach(System.out::println);
    }
}