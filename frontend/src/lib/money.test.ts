import { describe, expect, it } from "vitest";
import { formatMoney, sumAmounts } from "./money";

describe("money", () => {
  it("formats amounts as pounds and pence", () => {
    expect(formatMoney(1234.5)).toBe("£1,234.50");
    expect(formatMoney(0)).toBe("£0.00");
  });

  it("adds amounts without floating-point errors", () => {
    expect(0.1 + 0.2).not.toBe(0.3);
    expect(sumAmounts([0.1, 0.2])).toBe(0.3);
    expect(sumAmounts([12.5, 2.8, 40])).toBe(55.3);
  });
});
