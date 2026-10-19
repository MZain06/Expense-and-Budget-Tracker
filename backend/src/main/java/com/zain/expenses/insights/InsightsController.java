package com.zain.expenses.insights;

import com.zain.expenses.security.CurrentUserId;
import java.time.YearMonth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping
    public Insights insights(
            @CurrentUserId Long userId,
            @RequestParam(name = "month", required = false) YearMonth month) {
        return insightsService.insights(userId, month);
    }
}
