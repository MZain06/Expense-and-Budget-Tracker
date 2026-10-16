import { screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { fetchCategories } from "../api/categories";
import {
  createTransaction,
  deleteTransaction,
  fetchTransactions,
  updateTransaction,
} from "../api/transactions";
import { renderRoutes } from "../test/renderRoutes";
import type { Transaction } from "../types/api";
import { TransactionsPage } from "./TransactionsPage";

vi.mock("../api/categories");
vi.mock("../api/transactions");

const october: Transaction[] = [
  {
    id: 11,
    amount: 2.8,
    occurredOn: "2026-10-15",
    note: "Bus",
    categoryId: 2,
    categoryName: "Transport",
  },
  {
    id: 10,
    amount: 12.5,
    occurredOn: "2026-10-03",
    note: "Lunch",
    categoryId: 1,
    categoryName: "Food",
  },
];

function renderPage() {
  renderRoutes([{ path: "/", element: <TransactionsPage /> }], "/");
}

describe("TransactionsPage", () => {
  beforeEach(() => {
    // Pretend today is 16 October 2026, so the page opens on October.
    vi.useFakeTimers({ toFake: ["Date"] });
    vi.setSystemTime(new Date(2026, 9, 16, 12));
    vi.mocked(fetchCategories).mockResolvedValue([
      { id: 1, name: "Food" },
      { id: 2, name: "Transport" },
    ]);
    vi.mocked(fetchTransactions).mockResolvedValue(october);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("shows this month's transactions and the total spent", async () => {
    renderPage();

    expect(screen.getByRole("heading", { name: "October 2026" })).toBeInTheDocument();
    expect(await screen.findByText("Lunch")).toBeInTheDocument();
    expect(screen.getByText("Bus")).toBeInTheDocument();
    expect(screen.getByText(/spent across 2 transactions/)).toHaveTextContent(
      "£15.30 spent across 2 transactions",
    );
    expect(fetchTransactions).toHaveBeenCalledWith("2026-10", null);
  });

  it("moves to the previous month", async () => {
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Lunch");

    await user.click(screen.getByRole("button", { name: "Previous month" }));

    expect(screen.getByRole("heading", { name: "September 2026" })).toBeInTheDocument();
    expect(fetchTransactions).toHaveBeenLastCalledWith("2026-09", null);
  });

  it("filters by category", async () => {
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Lunch");

    await user.selectOptions(screen.getByLabelText("Filter by category"), "Food");

    expect(fetchTransactions).toHaveBeenLastCalledWith("2026-10", 1);
  });

  it("adds a transaction", async () => {
    vi.mocked(createTransaction).mockResolvedValue({ ...october[1], id: 12, note: "Coffee" });
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Lunch");

    await user.click(screen.getByRole("button", { name: "Add transaction" }));
    const category = screen.getByLabelText("Category");
    await within(category).findByRole("option", { name: "Food" });
    await user.type(screen.getByLabelText("Amount (£)"), "4.20");
    await user.selectOptions(category, "Food");
    await user.type(screen.getByLabelText("Note"), "Coffee");
    await user.click(screen.getByRole("button", { name: "Save transaction" }));

    expect(createTransaction).toHaveBeenCalledWith({
      amount: 4.2,
      categoryId: 1,
      occurredOn: "2026-10-16",
      note: "Coffee",
    });
    expect(screen.queryByRole("button", { name: "Save transaction" })).not.toBeInTheDocument();
  });

  it("checks the amount before saving", async () => {
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Lunch");

    await user.click(screen.getByRole("button", { name: "Add transaction" }));
    await user.type(screen.getByLabelText("Amount (£)"), "0");
    await user.click(screen.getByRole("button", { name: "Save transaction" }));

    expect(await screen.findByText("Enter an amount above zero")).toBeInTheDocument();
    expect(createTransaction).not.toHaveBeenCalled();
  });

  it("edits a transaction", async () => {
    vi.mocked(updateTransaction).mockResolvedValue({ ...october[1], amount: 13 });
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Lunch");

    await user.click(screen.getByRole("button", { name: "Edit Lunch" }));
    const amount = screen.getByLabelText("Amount (£)");
    expect(amount).toHaveValue("12.50");
    await user.clear(amount);
    await user.type(amount, "13.00");
    await user.click(screen.getByRole("button", { name: "Save changes" }));

    expect(updateTransaction).toHaveBeenCalledWith(10, {
      amount: 13,
      categoryId: 1,
      occurredOn: "2026-10-03",
      note: "Lunch",
    });
  });

  it("deletes a transaction after confirming", async () => {
    vi.spyOn(window, "confirm").mockReturnValue(true);
    vi.mocked(deleteTransaction).mockResolvedValue(undefined);
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Lunch");

    await user.click(screen.getByRole("button", { name: "Delete Lunch" }));

    expect(window.confirm).toHaveBeenCalledWith("Delete Lunch (£12.50)?");
    expect(deleteTransaction).toHaveBeenCalledWith(10);
  });

  it("explains when a month has no transactions", async () => {
    vi.mocked(fetchTransactions).mockResolvedValue([]);
    renderPage();

    expect(await screen.findByText("No transactions in October 2026.")).toBeInTheDocument();
  });
});
