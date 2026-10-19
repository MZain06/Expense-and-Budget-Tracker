const pounds = new Intl.NumberFormat("en-GB", { style: "currency", currency: "GBP" });

/** 1234.5 becomes "£1,234.50". */
export function formatMoney(amount: number): string {
  return pounds.format(amount);
}

const compactPounds = new Intl.NumberFormat("en-GB", {
  style: "currency",
  currency: "GBP",
  notation: "compact",
  minimumFractionDigits: 0,
  maximumFractionDigits: 1,
});

/** For chart axes: 1500 becomes "£1.5k" (or "£1.5K" with older locale data). */export function formatCompactMoney(amount: number): string {
  return compactPounds.format(amount);
}

/** Adds amounts in whole pence, so floating-point rounding errors can't creep into totals. */ export function sumAmounts(
  amounts: number[],
): number {
  const pence = amounts.reduce((total, amount) => total + Math.round(amount * 100), 0);
  return pence / 100;
}
