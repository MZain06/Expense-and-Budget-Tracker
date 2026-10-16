import { formatLongDate, formatWeekday, parseIsoDate } from "../lib/dates";
import { formatMoney } from "../lib/money";
import type { Transaction } from "../types/api";
import { Button } from "./Button";
import { PencilIcon, TrashIcon } from "./icons";

type TransactionListProps = {
  transactions: Transaction[];
  onEdit: (transaction: Transaction) => void;
  onDelete: (transaction: Transaction) => void;
};

type DayGroup = {
  date: string;
  transactions: Transaction[];
};

/** Groups transactions that are already sorted newest first into one group per day. */
function groupByDay(transactions: Transaction[]): DayGroup[] {
  const groups: DayGroup[] = [];
  for (const transaction of transactions) {
    const current = groups.at(-1);
    if (current && current.date === transaction.occurredOn) {
      current.transactions.push(transaction);
    } else {
      groups.push({ date: transaction.occurredOn, transactions: [transaction] });
    }
  }
  return groups;
}

/** A bank-statement style list: one block per day, newest day first. */
export function TransactionList({ transactions, onEdit, onDelete }: TransactionListProps) {
  return (
    <ol className="rounded-lg border border-line bg-surface px-4">
      {groupByDay(transactions).map((group) => (
        <li
          key={group.date}
          className="grid grid-cols-[2.75rem_minmax(0,1fr)] gap-4 border-b border-line py-4 last:border-b-0"
        >
          <div aria-hidden="true" className="text-center">
            <p className="text-2xl leading-none font-semibold tabular-nums">
              {parseIsoDate(group.date).getDate()}
            </p>
            <p className="mt-1 text-xs text-muted">{formatWeekday(group.date)}</p>
          </div>
          <div>
            <h2 className="sr-only">{formatLongDate(group.date)}</h2>
            <ul className="divide-y divide-line">
              {group.transactions.map((transaction) => {
                const description = transaction.note ?? transaction.categoryName;
                return (
                  <li
                    key={transaction.id}
                    className="flex items-center gap-2 py-2 first:pt-0 last:pb-0"
                  >
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-medium">{description}</p>
                      {transaction.note && (
                        <p className="text-sm text-muted">{transaction.categoryName}</p>
                      )}
                    </div>
                    <p className="font-medium tabular-nums">{formatMoney(transaction.amount)}</p>
                    <div className="flex">
                      <Button
                        variant="quiet"
                        size="icon"
                        aria-label={`Edit ${description}`}
                        title="Edit"
                        onClick={() => onEdit(transaction)}
                      >
                        <PencilIcon />
                      </Button>
                      <Button
                        variant="quietDanger"
                        size="icon"
                        aria-label={`Delete ${description}`}
                        title="Delete"
                        onClick={() => onDelete(transaction)}
                      >
                        <TrashIcon />
                      </Button>
                    </div>
                  </li>
                );
              })}
            </ul>
          </div>
        </li>
      ))}
    </ol>
  );
}
