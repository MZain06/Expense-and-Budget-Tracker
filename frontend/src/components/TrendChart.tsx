import { niceScale } from "../lib/chart";
import { formatMonth, formatShortMonth } from "../lib/dates";
import { formatCompactMoney, formatMoney } from "../lib/money";
import type { MonthTotal } from "../types/api";

type TrendChartProps = {
  /** Twelve months, oldest first. The last one is the month being viewed. */
  trend: MonthTotal[];
};

/**
 * A column per month with the month being viewed highlighted. The chart is for looking at;
 * the table underneath gives keyboard and screen-reader users the same numbers.
 */
export function TrendChart({ trend }: TrendChartProps) {
  const { max, ticks } = niceScale(Math.max(0, ...trend.map((point) => point.spent)));
  const heightOf = (value: number) => `${(value / max) * 100}%`;

  return (
    <section
      aria-labelledby="trend-heading"
      className="rounded-lg border border-line bg-surface p-5"
    >
      <h2 id="trend-heading" className="font-semibold">
        Last 12 months
      </h2>

      <div aria-hidden="true" className="mt-6 grid grid-cols-[2.75rem_minmax(0,1fr)] gap-x-3">
        {/* The labels are positioned absolutely, so this column needs a set width to hold them. */}
        <div className="relative h-40 text-right text-xs text-muted tabular-nums">
          {ticks.map((tick) => (
            <span
              key={tick}
              className="absolute right-0 translate-y-1/2"
              style={{ bottom: heightOf(tick) }}
            >
              {formatCompactMoney(tick)}
            </span>
          ))}
        </div>

        <div className="relative h-40">
          {ticks.map((tick) => (
            <div
              key={tick}
              className={`absolute inset-x-0 border-t ${tick === 0 ? "border-axis" : "border-grid"}`}
              style={{ bottom: heightOf(tick) }}
            />
          ))}
          <div className="relative flex h-full items-end">
            {trend.map((point, index) => {
              const isViewedMonth = index === trend.length - 1;
              return (
                <div
                  key={point.month}
                  className="group relative flex h-full flex-1 items-end justify-center"
                >
                  <div
                    className={`w-3/5 max-w-6 rounded-t-[4px] ${
                      isViewedMonth ? "bg-brand" : "bg-chart-muted group-hover:bg-muted"
                    }`}
                    style={{ height: heightOf(point.spent) }}
                  />
                  <div
                    className={`pointer-events-none absolute z-10 hidden rounded-md bg-ink px-2 py-1 text-xs whitespace-nowrap text-white group-hover:block ${tooltipPosition(index, trend.length)}`}
                    style={{ bottom: `calc(${heightOf(point.spent)} + 0.375rem)` }}
                  >
                    <span className="block font-semibold">{formatMoney(point.spent)}</span>
                    <span className="block text-white/75">{formatMonth(point.month)}</span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        <div />
        <div className="mt-2 flex text-center text-xs text-muted">
          {trend.map((point, index) => (
            <span
              key={point.month}
              // On narrow screens, label every other month, counting back from the one viewed.
              className={`flex-1 ${(trend.length - 1 - index) % 2 === 1 ? "invisible sm:visible" : ""}`}
            >
              {formatShortMonth(point.month)}
            </span>
          ))}
        </div>
      </div>

      <details className="mt-4 text-sm">
        <summary className="cursor-pointer text-muted hover:text-ink">Show as a table</summary>
        <table className="mt-3 w-full">
          <thead>
            <tr className="text-left text-muted">
              <th scope="col" className="py-1 font-medium">
                Month
              </th>
              <th scope="col" className="py-1 text-right font-medium">
                Spent
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-line">
            {trend.map((point) => (
              <tr key={point.month}>
                <td className="py-1.5">{formatMonth(point.month)}</td>
                <td className="py-1.5 text-right tabular-nums">{formatMoney(point.spent)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </details>
    </section>
  );
}

// Keep the tooltips of the first and last columns inside the card.
function tooltipPosition(index: number, count: number): string {
  if (index < 2) {
    return "left-0";
  }
  if (index >= count - 2) {
    return "right-0";
  }
  return "left-1/2 -translate-x-1/2";
}
