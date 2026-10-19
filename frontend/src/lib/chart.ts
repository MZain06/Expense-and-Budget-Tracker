/**
 * Rounds a chart's scale up to a tidy number and spaces the ticks evenly, so the axis reads
 * 0, 200, 400, 600, 800 rather than 0, 195.72, 391.44.
 */
export function niceScale(largest: number, roughTickCount = 4): { max: number; ticks: number[] } {
  if (largest <= 0) {
    return { max: 1, ticks: [0] };
  }
  const roughStep = largest / roughTickCount;
  const magnitude = 10 ** Math.floor(Math.log10(roughStep));
  const tidyStep =
    [1, 2, 2.5, 5, 10].map((multiple) => multiple * magnitude).find((step) => step >= roughStep) ??
    10 * magnitude;
  const step = Math.max(1, tidyStep);
  const tickCount = Math.ceil(largest / step);
  const ticks = Array.from({ length: tickCount + 1 }, (_, index) => index * step);
  return { max: tickCount * step, ticks };
}
