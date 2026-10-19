import { type FormEvent, useState } from "react";
import { type BudgetChange, useSaveBudgets } from "../hooks/useBudgets";
import { formatMoney, sumAmounts } from "../lib/money";
import type { Budget, Category } from "../types/api";
import { Button } from "./Button";

// The same rule as the backend's @Digits: up to 10 digits and 2 decimal places.
const AMOUNT = /^\d{1,10}(\.\d{1,2})?$/;

type BudgetFormProps = {
  month: string;
  categories: Category[];
  budgets: Budget[];
};

/** One row per category. Only the rows you change are saved; an empty row has no limit. */
export function BudgetForm({ month, categories, budgets }: BudgetFormProps) {
  const saved = new Map(budgets.map((budget) => [budget.categoryId, budget.limit]));
  const [drafts, setDrafts] = useState<Record<number, string>>({});
  const [errors, setErrors] = useState<Record<number, string>>({});
  const saveBudgets = useSaveBudgets(month);

  function shownValue(categoryId: number): string {
    return drafts[categoryId] ?? saved.get(categoryId)?.toFixed(2) ?? "";
  }

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const changes: BudgetChange[] = [];
    const problems: Record<number, string> = {};
    for (const [id, draft] of Object.entries(drafts)) {
      const categoryId = Number(id);
      const text = draft.trim();
      if (text === "") {
        if (saved.has(categoryId)) {
          changes.push({ categoryId, limit: null });
        }
      } else if (!AMOUNT.test(text)) {
        problems[categoryId] = "Enter an amount like 150 or 49.99";
      } else if (Number(text) !== saved.get(categoryId)) {
        changes.push({ categoryId, limit: Number(text) });
      }
    }
    setErrors(problems);
    if (Object.keys(problems).length > 0) {
      return;
    }
    if (changes.length === 0) {
      setDrafts({});
      return;
    }
    saveBudgets.mutate(changes, { onSuccess: () => setDrafts({}) });
  }

  const total = sumAmounts(budgets.map((budget) => budget.limit));

  return (
    <form onSubmit={handleSubmit} noValidate className="rounded-lg border border-line bg-surface">
      <ul className="divide-y divide-line px-4">
        {categories.map((category) => {
          const inputId = `budget-${category.id}`;
          const error = errors[category.id];
          return (
            <li
              key={category.id}
              className="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-x-4 gap-y-1 py-3"
            >
              <label htmlFor={inputId} className="truncate font-medium">
                {category.name}
                <span className="sr-only"> budget (£)</span>
              </label>
              <div className="flex items-center gap-1.5">
                <span aria-hidden="true" className="text-muted">
                  £
                </span>
                <input
                  id={inputId}
                  inputMode="decimal"
                  autoComplete="off"
                  placeholder="No limit"
                  value={shownValue(category.id)}
                  onChange={(event) => setDrafts({ ...drafts, [category.id]: event.target.value })}
                  aria-invalid={error ? true : undefined}
                  aria-describedby={error ? `${inputId}-error` : undefined}
                  className="w-28 rounded-md border border-line bg-surface px-3 py-2 text-right text-base tabular-nums aria-invalid:border-danger"
                />
              </div>
              {error && (
                <p id={`${inputId}-error`} className="col-span-2 text-sm text-danger">
                  {error}
                </p>
              )}
            </li>
          );
        })}
      </ul>
      <div className="flex flex-wrap items-center justify-between gap-3 border-t border-line px-4 py-3">
        <p className="text-sm text-muted">
          {budgets.length === 0
            ? "No budgets set yet."
            : `${formatMoney(total)} budgeted across ${budgets.length} ${
                budgets.length === 1 ? "category" : "categories"
              }`}
        </p>
        <Button type="submit" disabled={Object.keys(drafts).length === 0 || saveBudgets.isPending}>
          {saveBudgets.isPending ? "Saving…" : "Save budgets"}
        </Button>
        {saveBudgets.isError && (
          <p role="alert" className="w-full text-sm text-danger">
            Couldn't save every budget. Try again.
          </p>
        )}
      </div>
    </form>
  );
}
