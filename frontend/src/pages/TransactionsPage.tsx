import { useState } from "react";
import { Button } from "../components/Button";
import { MonthSwitcher } from "../components/MonthSwitcher";
import { TransactionForm } from "../components/TransactionForm";
import { TransactionList } from "../components/TransactionList";
import { useCategories } from "../hooks/useCategories";
import {
  useDeleteTransaction,
  useSaveTransaction,
  useTransactions,
} from "../hooks/useTransactions";
import { formatMonth, toMonth } from "../lib/dates";
import { formatMoney, sumAmounts } from "../lib/money";
import type { Transaction, TransactionInput } from "../types/api";

export function TransactionsPage() {
  const [month, setMonth] = useState(() => toMonth(new Date()));
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Transaction | "new" | null>(null);

  const categories = useCategories();
  const transactions = useTransactions(month, categoryId);
  const saveTransaction = useSaveTransaction();
  const deleteTransaction = useDeleteTransaction();

  const items = transactions.data ?? [];
  const total = sumAmounts(items.map((transaction) => transaction.amount));
  const categoryName = categories.data?.find((category) => category.id === categoryId)?.name;

  async function handleSave(input: TransactionInput) {
    const id = editing !== null && editing !== "new" ? editing.id : undefined;
    await saveTransaction.mutateAsync({ id, input });
    setEditing(null);
    // Jump to the month the transaction belongs to, so it's visible straight away.
    setMonth(input.occurredOn.slice(0, 7));
  }

  function handleDelete(transaction: Transaction) {
    const description = transaction.note ?? transaction.categoryName;
    if (window.confirm(`Delete ${description} (${formatMoney(transaction.amount)})?`)) {
      deleteTransaction.mutate(transaction.id);
    }
  }

  return (
    <div className="space-y-6">
      <section className="space-y-2">
        <MonthSwitcher month={month} onChange={setMonth} />
        {transactions.isSuccess && (
          <p className="text-muted">
            <span className="font-semibold text-ink tabular-nums">{formatMoney(total)}</span> spent
            {categoryName ? ` on ${categoryName}` : ""} across {items.length}{" "}
            {items.length === 1 ? "transaction" : "transactions"}
          </p>
        )}
      </section>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <select
          aria-label="Filter by category"
          value={categoryId ?? ""}
          onChange={(event) =>
            setCategoryId(event.target.value ? Number(event.target.value) : null)
          }
          className="rounded-md border border-line bg-surface px-3 py-2 text-sm"
        >
          <option value="">All categories</option>
          {categories.data?.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
        {editing === null && <Button onClick={() => setEditing("new")}>Add transaction</Button>}
      </div>

      {editing !== null && (
        <TransactionForm
          key={editing === "new" ? "new" : editing.id}
          categories={categories.data ?? []}
          initial={editing === "new" ? undefined : editing}
          onSave={handleSave}
          onCancel={() => setEditing(null)}
        />
      )}

      {deleteTransaction.isError && (
        <p role="alert" className="text-sm text-danger">
          Couldn't delete that transaction. Try again.
        </p>
      )}

      {transactions.isPending ? (
        <p className="text-muted">Loading transactions…</p>
      ) : transactions.isError ? (
        <div role="alert" className="space-y-3 rounded-lg border border-line bg-surface p-6">
          <p>Couldn't load your transactions.</p>
          <Button variant="secondary" onClick={() => transactions.refetch()}>
            Try again
          </Button>
        </div>
      ) : items.length === 0 ? (
        <div className="rounded-lg border border-dashed border-line px-6 py-10 text-center">
          <p className="font-medium">
            No {categoryName ? `${categoryName} ` : ""}transactions in {formatMonth(month)}.
          </p>
          <p className="mt-1 text-sm text-muted">Add one to start tracking your spending.</p>
        </div>
      ) : (
        <TransactionList transactions={items} onEdit={setEditing} onDelete={handleDelete} />
      )}
    </div>
  );
}
