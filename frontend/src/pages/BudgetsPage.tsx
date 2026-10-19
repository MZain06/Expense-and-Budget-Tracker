import { useState } from "react";
import { Link } from "react-router";
import { BudgetForm } from "../components/BudgetForm";
import { Button } from "../components/Button";
import { MonthSwitcher } from "../components/MonthSwitcher";
import { useBudgets, useCopyBudgets } from "../hooks/useBudgets";
import { useCategories } from "../hooks/useCategories";
import { formatMonthName, shiftMonth, toMonth } from "../lib/dates";

export function BudgetsPage() {
  const [month, setMonth] = useState(() => toMonth(new Date()));
  const previousMonth = shiftMonth(month, -1);
  const categories = useCategories();
  const budgets = useBudgets(month);
  const previousBudgets = useBudgets(previousMonth);
  const copyBudgets = useCopyBudgets(month);

  // Offer to copy last month's budgets when some of them aren't set this month yet.
  const alreadySet = new Set(budgets.data?.map((budget) => budget.categoryId));
  const canCopy =
    previousBudgets.data?.some((budget) => !alreadySet.has(budget.categoryId)) ?? false;

  return (
    <div className="space-y-6">
      <section className="space-y-2">
        <MonthSwitcher month={month} onChange={setMonth} />
        <p className="text-muted">
          Set a spending limit for any category. Leave it empty for no limit.
        </p>
      </section>

      {canCopy && (
        <Button
          variant="secondary"
          disabled={copyBudgets.isPending}
          onClick={() => copyBudgets.mutate()}
        >
          Copy {formatMonthName(previousMonth)}'s budgets
        </Button>
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
      ) : categories.data.length === 0 ? (
        <p className="rounded-lg border border-dashed border-line px-6 py-10 text-center text-muted">
          You don't have any categories yet.{" "}
          <Link to="/categories" className="font-medium text-brand hover:underline">
            Add one
          </Link>{" "}
          to set a budget.
        </p>
      ) : (
        <BudgetForm key={month} month={month} categories={categories.data} budgets={budgets.data} />
      )}
    </div>
  );
}
