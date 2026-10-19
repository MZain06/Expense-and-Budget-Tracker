import { Link } from "react-router";
import { formatMoney } from "../lib/money";
import type { CategorySummary } from "../types/api";
import { AlertIcon } from "./icons";

// From 80% of a budget, the meter turns amber as a warning.
const WARN_FROM = 0.8;

type BudgetMetersProps = {
  categories: CategorySummary[];
  monthName: string;
};

/** A progress bar for each budget, with the state written out so it never relies on colour. */
export function BudgetMeters({ categories, monthName }: BudgetMetersProps) {
  const budgeted = categories.filter((category) => category.budget !== null);

  return (
    <section
      aria-labelledby="budgets-heading"
      className="rounded-lg border border-line bg-surface p-5"
    >
      <h2 id="budgets-heading" className="font-semibold">
        Budgets
      </h2>
      {budgeted.length === 0 ? (
        <p className="mt-3 text-sm text-muted">
          No budgets for {monthName}.{" "}
          <Link to="/budgets" className="font-medium text-brand hover:underline">
            Set some
          </Link>
        </p>
      ) : (
        <ul className="mt-4 space-y-4">
          {budgeted.map((category) => (
            <BudgetMeter key={category.categoryId} category={category} />
          ))}
        </ul>
      )}
    </section>
  );
}

function BudgetMeter({ category }: { category: CategorySummary }) {
  const budget = category.budget ?? 0;
  const remaining = category.remaining ?? 0;
  const used = budget > 0 ? category.spent / budget : category.spent > 0 ? 1 : 0;
  const state = remaining < 0 ? "over" : used >= WARN_FROM ? "close" : "fine";
  const fill = { over: "bg-danger", close: "bg-warning-fill", fine: "bg-brand" }[state];
  const track = state === "close" ? "bg-warning-soft" : "bg-brand-soft";

  return (
    <li className="text-sm">
      <div className="flex items-baseline justify-between gap-3">
        <span className="truncate font-medium">{category.categoryName}</span>
        <span className="whitespace-nowrap text-muted tabular-nums">
          {formatMoney(category.spent)} of {formatMoney(budget)}
        </span>
      </div>
      <div aria-hidden="true" className={`mt-1.5 h-2 overflow-hidden rounded-full ${track}`}>
        <div
          className={`h-full rounded-full ${fill}`}
          style={{ width: `${Math.min(used, 1) * 100}%` }}
        />
      </div>
      {state === "over" ? (
        <p className="mt-1 flex items-center gap-1 text-danger">
          <AlertIcon /> Over by {formatMoney(-remaining)}
        </p>
      ) : state === "close" ? (
        <p className="mt-1 flex items-center gap-1 text-warning">
          <AlertIcon /> Only {formatMoney(remaining)} left
        </p>
      ) : (
        <p className="mt-1 text-muted">{formatMoney(remaining)} left</p>
      )}
    </li>
  );
}
