import { describe, expect, it } from "vitest";
import { niceScale } from "./chart";

describe("niceScale", () => {
  it("rounds the top up to a tidy number with evenly spaced ticks", () => {
    expect(niceScale(782.88)).toEqual({ max: 800, ticks: [0, 200, 400, 600, 800] });
    expect(niceScale(1516.83)).toEqual({ max: 2000, ticks: [0, 500, 1000, 1500, 2000] });
    expect(niceScale(90)).toEqual({ max: 100, ticks: [0, 25, 50, 75, 100] });
  });

  it("copes with a chart that has nothing in it", () => {
    expect(niceScale(0)).toEqual({ max: 1, ticks: [0] });
  });
});
