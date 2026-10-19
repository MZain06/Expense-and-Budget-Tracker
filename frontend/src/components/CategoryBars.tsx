import { formatMoney } from "../lib/money";
import type { CategorySummary } from "../types/api";

type CategoryBarsProps = {
  categories: CategorySummary[];
  monthName: string;
};

/** Spending per category, biggest first. Every amount is written out, so the bars are extra. */
export function CategoryBars({ categories, monthName }: CategoryBarsProps) {
  const spending = categories.filter((category) => category.spent > 0);
  const largest = Math.max(0, ...spending.map((category) => category.spent));

  return (
    <section
      aria-labelledby="where-heading"
      className="rounded-lg border border-line bg-surface p-5"
    >
      <h2 id="where-heading" className="font-semibold">
        Where it went
      </h2>
      {spending.length === 0 ? (
        <p className="mt-3 text-sm text-muted">No spending in {monthName}.</p>
      ) : (
        <ul className="mt-4 space-y-3">
          {spending.map((category) => (
            <li
              key={category.categoryId}
              className="grid grid-cols-[6.5rem_minmax(0,1fr)_auto] items-center gap-3 text-sm"
            >
              <span className="truncate">{category.categoryName}</span>
              <span
                aria-hidden="true"
                className="block h-3 min-w-0.5 rounded-r-[4px] bg-brand"
                style={{ width: `${(category.spent / largest) * 100}%` }}
              />
              <span className="text-right font-medium tabular-nums">
                {formatMoney(category.spent)}
              </span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
