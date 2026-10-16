// Dates travel as plain strings: months as "2026-10" and days as "2026-10-15".
// They are always read in local time, so a date never shifts by a day across time zones.

function pad(value: number): string {
  return String(value).padStart(2, "0");
}

export function toMonth(date: Date): string {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}`;
}

export function toIsoDate(date: Date): string {
  return `${toMonth(date)}-${pad(date.getDate())}`;
}

export function parseIsoDate(isoDate: string): Date {
  const [year, month, day] = isoDate.split("-").map(Number);
  return new Date(year, month - 1, day);
}

export function shiftMonth(month: string, delta: number): string {
  const [year, monthNumber] = month.split("-").map(Number);
  return toMonth(new Date(year, monthNumber - 1 + delta, 1));
}

/** "2026-10" becomes "October 2026". */
export function formatMonth(month: string): string {
  return new Intl.DateTimeFormat("en-GB", { month: "long", year: "numeric" }).format(
    parseIsoDate(`${month}-01`),
  );
}

/** "2026-10-15" becomes "Thu". */
export function formatWeekday(isoDate: string): string {
  return new Intl.DateTimeFormat("en-GB", { weekday: "short" }).format(parseIsoDate(isoDate));
}

/** "2026-10-15" becomes "Thursday 15 October". */
export function formatLongDate(isoDate: string): string {
  return new Intl.DateTimeFormat("en-GB", {
    weekday: "long",
    day: "numeric",
    month: "long",
  }).format(parseIsoDate(isoDate));
}
