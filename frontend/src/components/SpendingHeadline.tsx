import { formatMonthName, shiftMonth } from "../lib/dates";
import { formatMoney, sumAmounts } from "../lib/money";
import type { Insights } from "../types/api";
import { ArrowDownIcon, ArrowUpIcon } from "./icons";

type SpendingHeadlineProps = {
  month: string;
  insights: Insights;
};

/** The overview's lead number: what's been spent, and how it compares with last month. */
export function SpendingHeadline({ month, insights }: SpendingHeadlineProps) {
  const monthName = formatMonthName(month);
  const lastMonthName = formatMonthName(shiftMonth(month, -1));
  // The backend only projects a month that's still in progress.
  const inProgress = insights.projectedTotal !== null;
  const comparedWith = inProgress ? `at this point in ${lastMonthName}` : `in ${lastMonthName}`;
  const difference = sumAmounts([insights.spent, -insights.spentLastMonth]);

  return (
    <section
      aria-labelledby="spent-heading"
      className="rounded-lg border border-line bg-surface p-5 sm:p-6"
    >
      <h2 id="spent-heading" className="text-sm font-medium text-muted">
        {inProgress ? `Spent so far in ${monthName}` : `Spent in ${monthName}`}
      </h2>
      <p className="mt-1 text-5xl font-semibold tracking-tight">{formatMoney(insights.spent)}</p>
      {difference > 0 ? (
        <p className="mt-3 flex items-start gap-1.5 text-danger">
          <ArrowUpIcon className="mt-1 size-4 shrink-0" />
          <span>
            {formatMoney(difference)} more than {comparedWith}
          </span>
        </p>
      ) : difference < 0 ? (
        <p className="mt-3 flex items-start gap-1.5 text-brand">
          <ArrowDownIcon className="mt-1 size-4 shrink-0" />
          <span>
            {formatMoney(-difference)} less than {comparedWith}
          </span>
        </p>
      ) : (
        <p className="mt-3 text-muted">The same as {comparedWith}</p>
      )}
      {insights.projectedTotal !== null && insights.spent > 0 && (
        <p className="mt-1 text-sm text-muted">
          At this pace, you'll spend about {formatMoney(insights.projectedTotal)} by the end of{" "}
          {monthName}.
        </p>
      )}
    </section>
  );
}
