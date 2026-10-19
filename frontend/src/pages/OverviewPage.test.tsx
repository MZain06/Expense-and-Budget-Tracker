import { screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { fetchInsights } from "../api/insights";
import { fetchSummary } from "../api/summary";
import { renderRoutes } from "../test/renderRoutes";
import type { Insights, MonthlySummary } from "../types/api";
import { OverviewPage } from "./OverviewPage";

vi.mock("../api/summary");
vi.mock("../api/insights");

const summary: MonthlySummary = {
  totalSpent: 782.88,
  totalBudget: 225,
  categories: [
    { categoryId: 1, categoryName: "Bills", spent: 650, budget: null, remaining: null },
    { categoryId: 3, categoryName: "Food", spent: 107.5, budget: 200, remaining: 92.5 },
    { categoryId: 7, categoryName: "Transport", spent: 25.38, budget: 25, remaining: -0.38 },
  ],
};

const insights: Insights = {
  spent: 782.88,
  spentLastMonth: 662.48,
  averagePerDay: 48.93,
  projectedTotal: 1516.83,
  threeMonthAverage: 640.12,
  // November 2025 to October 2026. October's total matches `spent` above.
  trend: [
    { month: "2025-11", spent: 512.3 },
    { month: "2025-12", spent: 604.1 },
    { month: "2026-01", spent: 880.4 },
    { month: "2026-02", spent: 455 },
    { month: "2026-03", spent: 498.75 },
    { month: "2026-04", spent: 530.2 },
    { month: "2026-05", spent: 612.9 },
    { month: "2026-06", spent: 701.45 },
    { month: "2026-07", spent: 590 },
    { month: "2026-08", spent: 640.12 },
    { month: "2026-09", spent: 702.8 },
    { month: "2026-10", spent: 782.88 },
  ],
  biggestChanges: [
    { categoryId: 3, categoryName: "Food", thisMonth: 107.5, lastMonth: 62.1, change: 45.4 },
    { categoryId: 7, categoryName: "Transport", thisMonth: 25.38, lastMonth: 55.38, change: -30 },
  ],
};

function renderPage() {
  renderRoutes([{ path: "/", element: <OverviewPage /> }], "/");
}

describe("OverviewPage", () => {
  beforeEach(() => {
    // Pretend today is 16 October 2026, so the page opens on October.
    vi.useFakeTimers({ toFake: ["Date"] });
    vi.setSystemTime(new Date(2026, 9, 16, 12));
    vi.mocked(fetchSummary).mockResolvedValue(summary);
    vi.mocked(fetchInsights).mockResolvedValue(insights);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("leads with this month's spending and how it compares", async () => {
    renderPage();

    const headline = await screen.findByRole("region", { name: "Spent so far in October" });
    expect(within(headline).getByText("£782.88")).toBeInTheDocument();
    expect(
      within(headline).getByText("£120.40 more than at this point in September"),
    ).toBeInTheDocument();
    expect(within(headline).getByText(/about £1,516.83 by the end of October/)).toBeInTheDocument();
    expect(fetchSummary).toHaveBeenCalledWith("2026-10");
  });

  it("shows where the money went and how each budget is doing", async () => {
    renderPage();

    const where = await screen.findByRole("region", { name: "Where it went" });
    expect(within(where).getByText("Bills")).toBeInTheDocument();
    expect(within(where).getByText("£650.00")).toBeInTheDocument();
    const budgets = screen.getByRole("region", { name: "Budgets" });
    expect(within(budgets).getByText("£92.50 left")).toBeInTheDocument();
    expect(within(budgets).getByText("Over by £0.38")).toBeInTheDocument();
    expect(screen.getByText("£92.12 left")).toBeInTheDocument();
  });

  it("lists the last 12 months in a table", async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByText("Show as a table"));

    const table = screen.getByRole("table");
    expect(within(table).getAllByRole("row")).toHaveLength(13);
    expect(within(table).getByRole("cell", { name: "November 2025" })).toBeInTheDocument();
    expect(within(table).getByRole("cell", { name: "£512.30" })).toBeInTheDocument();
  });

  it("names the categories that changed most", async () => {
    renderPage();

    const changes = await screen.findByRole("region", { name: "Biggest changes" });
    expect(within(changes).getByText("Compared with this point in September")).toBeInTheDocument();
    expect(within(changes).getByText("£45.40 more")).toBeInTheDocument();
    expect(within(changes).getByText("£30.00 less")).toBeInTheDocument();
  });

  it("explains a month with nothing in it", async () => {
    vi.mocked(fetchSummary).mockResolvedValue({ totalSpent: 0, totalBudget: 0, categories: [] });
    vi.mocked(fetchInsights).mockResolvedValue({
      ...insights,
      spent: 0,
      spentLastMonth: 0,
      averagePerDay: 0,
      biggestChanges: [],
    });
    renderPage();

    expect(await screen.findByText("No spending in October.")).toBeInTheDocument();
    expect(screen.getByText(/No budgets for October/)).toBeInTheDocument();
    expect(screen.getByText("No category has changed.")).toBeInTheDocument();
  });
});
