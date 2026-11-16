import { Link } from "react-router";
import { formatMoney, sumAmounts } from "../lib/money";
import type { MonthlySummary } from "../types/api";
import { Button } from "./Button";

type IncomePanelProps = {
  summary: MonthlySummary;
  monthName: string;
  inProgress: boolean;
  onHide: () => void;
};

/** The month's money in, and what's left of it after spending. */
export function IncomePanel({ summary, monthName, inProgress, onHide }: IncomePanelProps) {
  const leftOver = sumAmounts([summary.totalIncome, -summary.totalSpent]);

  return (
    <section
      aria-labelledby="income-heading"
      className="rounded-lg border border-line bg-surface p-5"
    >
      <div className="flex items-start justify-between gap-3">
        <h2 id="income-heading" className="font-semibold">
          Money in
        </h2>
        <Button variant="quiet" aria-label="Hide money in" className="-my-2 -mr-3" onClick={onHide}>
          Hide
        </Button>
      </div>
      {summary.income.length === 0 ? (
        <p className="mt-2 text-sm text-muted">
          Nothing received in {monthName}
          {inProgress ? " yet" : ""}. Add your pay as money in on{" "}
          <Link to="/transactions" className="font-medium text-brand hover:underline">
            Transactions
          </Link>{" "}
          to see what's left after spending.
        </p>
      ) : (
        <>
          <p className="mt-1 text-2xl font-semibold tracking-tight">
            {formatMoney(summary.totalIncome)}
          </p>
          {leftOver >= 0 ? (
            <p className="mt-1 text-sm text-muted">
              {formatMoney(leftOver)} left after spending{inProgress ? " so far" : ""}
            </p>
          ) : (
            <p className="mt-1 text-sm text-danger">
              {formatMoney(-leftOver)} more spent than came in
            </p>
          )}
          {summary.income.length > 1 && (
            <ul className="mt-3 space-y-1 text-sm">
              {summary.income.map((category) => (
                <li key={category.categoryId} className="flex justify-between gap-3">
                  <span className="truncate">{category.categoryName}</span>
                  <span className="tabular-nums">{formatMoney(category.received)}</span>
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </section>
  );
}
