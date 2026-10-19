import { describe, expect, it } from "vitest";
import { formatCompactMoney, formatMoney, sumAmounts } from "./money";
describe("money", () => {
  it("formats amounts as pounds and pence", () => {
    expect(formatMoney(1234.5)).toBe("£1,234.50");
    expect(formatMoney(0)).toBe("£0.00");
  });

  it("shortens amounts for chart axes", () => {
    expect(formatCompactMoney(0)).toBe("£0");
    expect(formatCompactMoney(800)).toBe("£800");
    expect(formatCompactMoney(1500)).toBe("£1.5K");
  });

  it("adds amounts without floating-point errors", () => {
    expect(0.1 + 0.2).not.toBe(0.3);
    expect(sumAmounts([0.1, 0.2])).toBe(0.3);
    expect(sumAmounts([12.5, 2.8, 40])).toBe(55.3);
  });
});
