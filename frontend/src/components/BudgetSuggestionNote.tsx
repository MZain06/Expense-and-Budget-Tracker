import { formatMonthName } from "../lib/dates";
import { formatMoney, sumAmounts } from "../lib/money";
import type { BudgetSuggestions } from "../types/api";
import { Button } from "./Button";

type BudgetSuggestionNoteProps = {
  suggestions: BudgetSuggestions;
  onDiscard: () => void;
};

/** Explains where suggested budgets come from and how they compare with income. */
export function BudgetSuggestionNote({ suggestions, onDiscard }: BudgetSuggestionNoteProps) {
  const { usualSpending, usualIncome, total } = suggestions;
  const leftOver = sumAmounts([usualIncome, -total]);

  return (
    <section
      aria-labelledby="suggestions-heading"
      className="rounded-lg border border-brand/40 bg-brand-soft p-4 text-sm sm:p-5"
    >
      <h2 id="suggestions-heading" className="font-semibold">
        Suggested from {describeMonths(suggestions.basedOn)}
      </h2>
      <p className="mt-1">
        You usually spend {formatMoney(usualSpending)} a month
        {usualIncome > 0 ? ` and receive ${formatMoney(usualIncome)}` : ""}. These budgets add up to{" "}
        {formatMoney(total)}
        {usualIncome === 0
          ? "."
          : leftOver >= 0
            ? `, which leaves about ${formatMoney(leftOver)}.`
            : `, ${formatMoney(-leftOver)} more than you usually receive.`}
      </p>
      <p className="mt-1 text-muted">
        Check them below and change any you like. They aren't saved until you press Save budgets.
      </p>
      <Button variant="secondary" className="mt-3" onClick={onDiscard}>
        Discard suggestions
      </Button>
    </section>
  );
}

// The months are consecutive when there are three, but two can have a gap between them.
function describeMonths(months: string[]): string {
  const names = months.map(formatMonthName);
  if (names.length === 3) {
    return `${names[0]} to ${names[2]}`;
  }
  return names.join(" and ");
}
