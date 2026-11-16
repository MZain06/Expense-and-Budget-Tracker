import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import {
  copyBudgetsFromPreviousMonth,
  fetchBudgetSuggestions,
  fetchBudgets,
  removeBudget,
  setBudget,
} from "../api/budgets";
import { fetchCategories } from "../api/categories";
import { renderRoutes } from "../test/renderRoutes";
import type { BudgetSuggestions } from "../types/api";
import { BudgetsPage } from "./BudgetsPage";

vi.mock("../api/budgets");
vi.mock("../api/categories");

function renderPage() {
  renderRoutes([{ path: "/", element: <BudgetsPage /> }], "/");
}

const suggestions: BudgetSuggestions = {
  basedOn: ["2026-07", "2026-08", "2026-09"],
  usualSpending: 264.71,
  usualIncome: 1350,
  total: 270,
  budgets: [
    { categoryId: 1, categoryName: "Food", usualSpending: 212.3, limit: 215 },
    { categoryId: 2, categoryName: "Transport", usualSpending: 52.41, limit: 55 },
  ],
};

describe("BudgetsPage", () => {
  beforeEach(() => {
    // Pretend today is 16 October 2026, so the page opens on October.
    vi.useFakeTimers({ toFake: ["Date"] });
    vi.setSystemTime(new Date(2026, 9, 16, 12));
    vi.mocked(fetchCategories).mockResolvedValue([
      { id: 1, name: "Food", kind: "SPENDING" },
      { id: 2, name: "Transport", kind: "SPENDING" },
      { id: 3, name: "Salary", kind: "INCOME" },
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
    expect(screen.queryByLabelText("Salary budget (£)")).not.toBeInTheDocument();
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

  it("fills in suggested budgets to check before saving them", async () => {
    vi.mocked(fetchBudgetSuggestions).mockResolvedValue(suggestions);
    vi.mocked(setBudget).mockImplementation(async (_month, categoryId, limit) => ({
      categoryId,
      categoryName: categoryId === 1 ? "Food" : "Transport",
      limit,
    }));
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole("button", { name: "Suggest budgets" }));

    const note = await screen.findByRole("region", { name: "Suggested from July to September" });
    expect(note).toHaveTextContent(
      "You usually spend £264.71 a month and receive £1,350.00. These budgets add up to £270.00, which leaves about £1,080.00.",
    );
    expect(fetchBudgetSuggestions).toHaveBeenCalledWith("2026-10");
    expect(screen.getByLabelText("Food budget (£)")).toHaveValue("215.00");
    expect(screen.getByLabelText("Transport budget (£)")).toHaveValue("55.00");
    expect(setBudget).not.toHaveBeenCalled();

    await user.click(screen.getByRole("button", { name: "Save budgets" }));

    expect(setBudget).toHaveBeenCalledWith("2026-10", 1, 215);
    expect(setBudget).toHaveBeenCalledWith("2026-10", 2, 55);
    await waitFor(() => expect(note).not.toBeInTheDocument());
  });

  it("puts the saved budgets back when suggestions are discarded", async () => {
    vi.mocked(fetchBudgetSuggestions).mockResolvedValue(suggestions);
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole("button", { name: "Suggest budgets" }));
    await user.click(await screen.findByRole("button", { name: "Discard suggestions" }));

    expect(screen.getByLabelText("Food budget (£)")).toHaveValue("200.00");
    expect(screen.getByLabelText("Transport budget (£)")).toHaveValue("");
    expect(screen.getByRole("button", { name: "Suggest budgets" })).toBeInTheDocument();
  });

  it("says when suggested budgets add up to more than usually comes in", async () => {
    vi.mocked(fetchBudgetSuggestions).mockResolvedValue({
      ...suggestions,
      basedOn: ["2026-08", "2026-09"],
      usualIncome: 200,
    });
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole("button", { name: "Suggest budgets" }));

    expect(
      await screen.findByRole("region", { name: "Suggested from August and September" }),
    ).toHaveTextContent("These budgets add up to £270.00, £70.00 more than you usually receive.");
  });

  it("says when there's no recent spending to suggest budgets from", async () => {
    vi.mocked(fetchBudgetSuggestions).mockResolvedValue({
      basedOn: [],
      usualSpending: 0,
      usualIncome: 0,
      total: 0,
      budgets: [],
    });
    const user = userEvent.setup();
    renderPage();

    await user.click(await screen.findByRole("button", { name: "Suggest budgets" }));

    expect(
      await screen.findByText(
        "There's no spending in the three months before October to suggest budgets from.",
      ),
    ).toBeInTheDocument();
    expect(screen.getByLabelText("Food budget (£)")).toHaveValue("200.00");
  });
});
