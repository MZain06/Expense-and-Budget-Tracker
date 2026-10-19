import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import {
  copyBudgetsFromPreviousMonth,
  fetchBudgets,
  removeBudget,
  setBudget,
} from "../api/budgets";
import { fetchCategories } from "../api/categories";
import { renderRoutes } from "../test/renderRoutes";
import { BudgetsPage } from "./BudgetsPage";

vi.mock("../api/budgets");
vi.mock("../api/categories");

function renderPage() {
  renderRoutes([{ path: "/", element: <BudgetsPage /> }], "/");
}

describe("BudgetsPage", () => {
  beforeEach(() => {
    // Pretend today is 16 October 2026, so the page opens on October.
    vi.useFakeTimers({ toFake: ["Date"] });
    vi.setSystemTime(new Date(2026, 9, 16, 12));
    vi.mocked(fetchCategories).mockResolvedValue([
      { id: 1, name: "Food" },
      { id: 2, name: "Transport" },
    ]);
    vi.mocked(fetchBudgets).mockImplementation(async (month) =>
      month === "2026-10" ? [{ categoryId: 1, categoryName: "Food", limit: 200 }] : [],
    );
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("shows this month's budgets and their total", async () => {
    renderPage();

    expect(await screen.findByLabelText("Food budget (£)")).toHaveValue("200.00");
    expect(screen.getByLabelText("Transport budget (£)")).toHaveValue("");
    expect(screen.getByText("£200.00 budgeted across 1 category")).toBeInTheDocument();
  });

  it("saves only the budgets that changed", async () => {
    vi.mocked(setBudget).mockResolvedValue({
      categoryId: 2,
      categoryName: "Transport",
      limit: 50,
    });
    vi.mocked(removeBudget).mockResolvedValue(undefined);
    const user = userEvent.setup();
    renderPage();

    await user.clear(await screen.findByLabelText("Food budget (£)"));
    await user.type(screen.getByLabelText("Transport budget (£)"), "50");
    await user.click(screen.getByRole("button", { name: "Save budgets" }));

    expect(removeBudget).toHaveBeenCalledWith("2026-10", 1);
    expect(setBudget).toHaveBeenCalledWith("2026-10", 2, 50);
    expect(setBudget).toHaveBeenCalledTimes(1);
  });

  it("checks amounts before saving", async () => {
    const user = userEvent.setup();
    renderPage();

    await user.type(await screen.findByLabelText("Transport budget (£)"), "12.345");
    await user.click(screen.getByRole("button", { name: "Save budgets" }));

    expect(await screen.findByText("Enter an amount like 150 or 49.99")).toBeInTheDocument();
    expect(setBudget).not.toHaveBeenCalled();
  });

  it("copies last month's budgets", async () => {
    vi.mocked(fetchBudgets).mockImplementation(async (month) =>
      month === "2026-09" ? [{ categoryId: 2, categoryName: "Transport", limit: 50 }] : [],
    );
    vi.mocked(copyBudgetsFromPreviousMonth).mockResolvedValue([]);
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole("button", { name: "Copy September's budgets" }));

    expect(copyBudgetsFromPreviousMonth).toHaveBeenCalledWith("2026-10");
  });
});
