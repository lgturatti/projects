package batch.reports;

import java.util.ArrayList;
import java.util.List;

public class BatchReportService {

    public List<FinancialReport> generateDailyReports() {

        List<FinancialReport> reports =
                new ArrayList<>();

        reports.add(
                new FinancialReport(
                        "Rock Festival",
                        120000.0,
                        3000
                )
        );

        reports.add(
                new FinancialReport(
                        "Tech Conference",
                        85000.0,
                        1200
                )
        );

        return reports;
    }
}