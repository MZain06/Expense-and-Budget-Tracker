import { useState } from "react";
import { Link } from "react-router";
import { BiggestChanges } from "../components/BiggestChanges";
import { BudgetMeters } from "../components/BudgetMeters";
import { Button } from "../components/Button";
import { CategoryBars } from "../components/CategoryBars";
import { IncomePanel } from "../components/IncomePanel";
import { MonthSwitcher } from "../components/MonthSwitcher";
import { SpendingHeadline } from "../components/SpendingHeadline";
import { StatTile } from "../components/StatTile";
import { TrendChart } from "../components/TrendChart";
import { useInsights, useSummary } from "../hooks/useOverview";
import { useShowIncome } from "../hooks/useShowIncome";
import { formatMonthName, shiftMonth, toMonth } from "../lib/dates";
import { formatMoney, sumAmounts } from "../lib/money";
import type { Insights, MonthlySummary } from "../types/api";

export function OverviewPage() {
  const [month, setMonth] = useState(() => toMonth(new Date()));
  const [showIncome, setShowIncome] = useShowIncome();
  const summary = useSummary(month);
  const insights = useInsights(month);

  return (
    <div className="space-y-6">
      <MonthSwitcher month={month} onChange={setMonth} />
      {summary.isPending || insights.isPending ? (
        <p className="text-muted">Loading your overview…</p>
      ) : summary.isError || insights.isError ? (
        <div role="alert" className="space-y-3 rounded-lg border border-line bg-surface p-6">
          <p>Couldn't load your overview.</p>
          <Button
            variant="secondary"
            onClick={() => {
              summary.refetch();
              insights.refetch();
            }}
          >
            Try again
          </Button>
        </div>
      ) : (
        <Overview
          month={month}
          summary={summary.data}
          insights={insights.data}
          showIncome={showIncome}
          onShowIncome={setShowIncome}
        />
      )}
    </div>
  );
}

type OverviewProps = {
  month: string;
  summary: MonthlySummary;
  insights: Insights;
  showIncome: boolean;
  onShowIncome: (show: boolean) => void;
};

function Overview({ month, summary, insights, showIncome, onShowIncome }: OverviewProps) {
  const monthName = formatMonthName(month);
  const lastMonthName = formatMonthName(shiftMonth(month, -1));
  const inProgress = insights.projectedTotal !== null;
  const budgeted = summary.categories.filter((category) => category.budget !== null);
  const budgetLeft = sumAmounts(budgeted.map((category) => category.remaining ?? 0));

  return (
    <>
      <SpendingHeadline month={month} insights={insights} />

      {showIncome ? (
        <IncomePanel
          summary={summary}
          monthName={monthName}
          inProgress={inProgress}
          onHide={() => onShowIncome(false)}
        />
      ) : (
        <div className="-mt-3 flex justify-end">
          <Button variant="quiet" onClick={() => onShowIncome(true)}>
            Show money in
          </Button>
        </div>
      )}

      <div className="grid gap-3 sm:grid-cols-3">
        <StatTile
          label="Average per day"
          value={formatMoney(insights.averagePerDay)}
          detail={inProgress ? "So far this month" : `Across ${monthName}`}
        />
        <StatTile
          label="Usual month"
          value={formatMoney(insights.threeMonthAverage)}
          detail={`Average of ${formatMonthName(shiftMonth(month, -3))} to ${lastMonthName}`}
        />
        {budgeted.length === 0 ? (
          <StatTile
            label="Budgets"
            value="None set"
            detail={
              <Link to="/budgets" className="font-medium text-brand hover:underline">
                Set a budget
              </Link>
            }
          />
        ) : (
          <StatTile
            label="Budgets"
            value={
              budgetLeft < 0
                ? `${formatMoney(-budgetLeft)} over`
                : `${formatMoney(budgetLeft)} left`
            }
            detail={`From ${formatMoney(summary.totalBudget)} across ${budgeted.length} ${
              budgeted.length === 1 ? "budget" : "budgets"
            }`}
          />
        )}
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <CategoryBars categories={summary.categories} monthName={monthName} />
        <BudgetMeters categories={summary.categories} monthName={monthName} />
      </div>

      <TrendChart trend={insights.trend} />

      <BiggestChanges
        changes={insights.biggestChanges}
        comparedWith={inProgress ? `this point in ${lastMonthName}` : lastMonthName}
      />
    </>
  );
}
