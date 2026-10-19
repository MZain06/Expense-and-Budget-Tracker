import { formatMoney } from "../lib/money";
import type { CategoryChange } from "../types/api";
import { ArrowDownIcon, ArrowUpIcon } from "./icons";

type BiggestChangesProps = {
  changes: CategoryChange[];
  /** What this month is being compared with, like "August" or "this point in September". */
  comparedWith: string;
};

/** The categories whose spending moved most since last month. */
export function BiggestChanges({ changes, comparedWith }: BiggestChangesProps) {
  return (
    <section
      aria-labelledby="changes-heading"
      className="rounded-lg border border-line bg-surface p-5"
    >
      <h2 id="changes-heading" className="font-semibold">
        Biggest changes
      </h2>
      <p className="mt-1 text-sm text-muted">Compared with {comparedWith}</p>
      {changes.length === 0 ? (
        <p className="mt-3 text-sm text-muted">No category has changed.</p>
      ) : (
        <ul className="mt-3 divide-y divide-line">
          {changes.map((change) => (
            <li
              key={change.categoryId}
              className="flex items-center justify-between gap-3 py-2.5 text-sm"
            >
              <div className="min-w-0">
                <p className="truncate font-medium">{change.categoryName}</p>
                <p className="text-muted">
                  {formatMoney(change.thisMonth)}, from {formatMoney(change.lastMonth)}
                </p>
              </div>
              {change.change > 0 ? (
                <p className="flex shrink-0 items-center gap-1 text-danger">
                  <ArrowUpIcon />
                  {formatMoney(change.change)} more
                </p>
              ) : (
                <p className="flex shrink-0 items-center gap-1 text-brand">
                  <ArrowDownIcon />
                  {formatMoney(-change.change)} less
                </p>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
