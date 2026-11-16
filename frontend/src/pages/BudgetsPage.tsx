import { useState } from "react";
import { Link } from "react-router";
import { BudgetForm } from "../components/BudgetForm";
import { BudgetSuggestionNote } from "../components/BudgetSuggestionNote";
import { Button } from "../components/Button";
import { MonthSwitcher } from "../components/MonthSwitcher";
import { useBudgetSuggestions, useBudgets, useCopyBudgets } from "../hooks/useBudgets";
import { useCategories } from "../hooks/useCategories";
import { formatMonthName, shiftMonth, toMonth } from "../lib/dates";

export function BudgetsPage() {
  const [month, setMonth] = useState(() => toMonth(new Date()));
  const [suggesting, setSuggesting] = useState(false);
  const previousMonth = shiftMonth(month, -1);
  const categories = useCategories();
  const budgets = useBudgets(month);
  const previousBudgets = useBudgets(previousMonth);
  const copyBudgets = useCopyBudgets(month);
  const suggestions = useBudgetSuggestions(month, suggesting);
  const spending = categories.data?.filter((category) => category.kind === "SPENDING") ?? [];

  // Offer to copy last month's budgets when some of them aren't set this month yet.
  const alreadySet = new Set(budgets.data?.map((budget) => budget.categoryId));
  const canCopy =
    previousBudgets.data?.some((budget) => !alreadySet.has(budget.categoryId)) ?? false;
  const canSuggest = spending.length > 0;
  const suggested =
    suggesting && suggestions.isSuccess && suggestions.data.budgets.length > 0
      ? suggestions.data.budgets
      : undefined;

  function changeMonth(next: string) {
    setMonth(next);
    setSuggesting(false);
  }

  return (
    <div className="space-y-6">
      <section className="space-y-2">
        <MonthSwitcher month={month} onChange={changeMonth} />
        <p className="text-muted">
          Set a spending limit for any category. Leave it empty for no limit.
        </p>
      </section>

      {suggesting ? (
        suggestions.isPending ? (
          <p className="text-muted">Working out suggestions…</p>
        ) : suggestions.isError ? (
          <div
            role="alert"
            className="flex flex-wrap items-center gap-3 rounded-lg border border-line bg-surface p-4 text-sm"
          >
            <p className="flex-1">Couldn't suggest budgets.</p>
            <Button variant="secondary" onClick={() => suggestions.refetch()}>
              Try again
            </Button>
            <Button variant="quiet" onClick={() => setSuggesting(false)}>
              Close
            </Button>
          </div>
        ) : suggestions.data.budgets.length === 0 ? (
          <div
            role="status"
            className="flex flex-wrap items-center gap-3 rounded-lg border border-line bg-surface p-4 text-sm"
          >
            <p className="flex-1">
              There's no spending in the three months before {formatMonthName(month)} to suggest
              budgets from.
            </p>
            <Button variant="secondary" onClick={() => setSuggesting(false)}>
              Close
            </Button>
          </div>
        ) : (
          <BudgetSuggestionNote
            suggestions={suggestions.data}
            onDiscard={() => setSuggesting(false)}
          />
        )
      ) : (
        (canCopy || canSuggest) && (
          <div className="flex flex-wrap gap-3">
            {canCopy && (
              <Button
                variant="secondary"
                disabled={copyBudgets.isPending}
                onClick={() => copyBudgets.mutate()}
              >
                Copy {formatMonthName(previousMonth)}'s budgets
              </Button>
            )}
            {canSuggest && (
              <Button variant="secondary" onClick={() => setSuggesting(true)}>
                Suggest budgets
              </Button>
            )}
          </div>
        )
      )}
      {copyBudgets.isError && (
        <p role="alert" className="text-sm text-danger">
          Couldn't copy last month's budgets. Try again.
        </p>
      )}

      {categories.isPending || budgets.isPending ? (
        <p className="text-muted">Loading budgets…</p>
      ) : categories.isError || budgets.isError ? (
        <div role="alert" className="space-y-3 rounded-lg border border-line bg-surface p-6">
          <p>Couldn't load your budgets.</p>
          <Button
            variant="secondary"
            onClick={() => {
              categories.refetch();
              budgets.refetch();
            }}
          >
            Try again
          </Button>
        </div>
      ) : spending.length === 0 ? (
        <p className="rounded-lg border border-dashed border-line px-6 py-10 text-center text-muted">
          You don't have any spending categories yet.{" "}
          <Link to="/categories" className="font-medium text-brand hover:underline">
            Add one
          </Link>{" "}
          to set a budget.
        </p>
      ) : (
        <BudgetForm
          key={suggested ? `${month}-suggested` : month}
          month={month}
          categories={spending}
          budgets={budgets.data}
          suggested={suggested}
          onSaved={() => setSuggesting(false)}
        />
      )}
    </div>
  );
}
