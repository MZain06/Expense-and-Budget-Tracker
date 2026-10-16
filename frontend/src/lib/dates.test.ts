import { describe, expect, it } from "vitest";
import { formatLongDate, formatMonth, shiftMonth, toIsoDate, toMonth } from "./dates";

describe("dates", () => {
  it("formats a date as an ISO day and month", () => {
    const date = new Date(2026, 9, 5);
    expect(toIsoDate(date)).toBe("2026-10-05");
    expect(toMonth(date)).toBe("2026-10");
  });

  it("moves between months across year boundaries", () => {
    expect(shiftMonth("2026-12", 1)).toBe("2027-01");
    expect(shiftMonth("2026-01", -1)).toBe("2025-12");
  });

  it("formats months and days for people to read", () => {
    expect(formatMonth("2026-10")).toBe("October 2026");
    expect(formatLongDate("2026-10-15")).toBe("Thursday 15 October");
  });
});
