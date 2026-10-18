package com.zain.expenses.summary;

import com.zain.expenses.security.CurrentUserId;
import java.time.YearMonth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping
    public MonthlySummary summary(
            @CurrentUserId Long userId,
            @RequestParam(name = "month", required = false) YearMonth month) {
        return summaryService.summarise(userId, month);
    }
}
