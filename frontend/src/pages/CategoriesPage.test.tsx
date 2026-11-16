import { screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { createCategory, deleteCategory, fetchCategories, updateCategory } from "../api/categories";
import { ApiError } from "../api/client";
import { renderRoutes } from "../test/renderRoutes";
import { CategoriesPage } from "./CategoriesPage";

vi.mock("../api/categories");

function renderPage() {
  renderRoutes([{ path: "/", element: <CategoriesPage /> }], "/");
}

describe("CategoriesPage", () => {
  beforeEach(() => {
    vi.mocked(fetchCategories).mockResolvedValue([
      { id: 1, name: "Food", kind: "SPENDING" },
      { id: 2, name: "Transport", kind: "SPENDING" },
      { id: 3, name: "Salary", kind: "INCOME" },
    ]);
  });

  it("lists spending and income categories separately", async () => {
    renderPage();

    const spending = await screen.findByRole("region", { name: "Spending" });
    expect(within(spending).getByText("Food")).toBeInTheDocument();
    expect(within(spending).getByText("Transport")).toBeInTheDocument();
    const income = screen.getByRole("region", { name: "Income" });
    expect(within(income).getByText("Salary")).toBeInTheDocument();
  });

  it("adds a category and clears the field", async () => {
    vi.mocked(createCategory).mockResolvedValue({ id: 4, name: "Coffee", kind: "SPENDING" });
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Food");

    await user.type(screen.getByLabelText("New category"), "Coffee");
    await user.click(screen.getByRole("button", { name: "Add category" }));

    expect(createCategory).toHaveBeenCalledWith("Coffee", "SPENDING");
    await waitFor(() => expect(screen.getByLabelText("New category")).toHaveValue(""));
  });

  it("adds an income category", async () => {
    vi.mocked(createCategory).mockResolvedValue({ id: 4, name: "Freelance", kind: "INCOME" });
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Food");

    const form = screen.getByRole("form", { name: "Add a category" });
    await user.type(within(form).getByLabelText("New category"), "Freelance");
    await user.selectOptions(within(form).getByLabelText("Type"), "Income");
    await user.click(within(form).getByRole("button", { name: "Add category" }));

    expect(createCategory).toHaveBeenCalledWith("Freelance", "INCOME");
  });

  it("shows the server's message when the name is taken", async () => {
    vi.mocked(createCategory).mockRejectedValue(
      new ApiError(409, "You already have a category called food"),
    );
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Food");

    await user.type(screen.getByLabelText("New category"), "food");
    await user.click(screen.getByRole("button", { name: "Add category" }));

    expect(await screen.findByText("You already have a category called food")).toBeInTheDocument();
  });

  it("renames a category", async () => {
    vi.mocked(updateCategory).mockResolvedValue({ id: 1, name: "Groceries", kind: "SPENDING" });
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Food");

    await user.click(screen.getByRole("button", { name: "Edit Food" }));
    const form = screen.getByRole("form", { name: "Edit Food" });
    const name = within(form).getByLabelText("Name");
    expect(name).toHaveValue("Food");
    await user.clear(name);
    await user.type(name, "Groceries");
    await user.click(within(form).getByRole("button", { name: "Save" }));

    expect(updateCategory).toHaveBeenCalledWith(1, "Groceries", "SPENDING");
  });

  it("explains why a category can't be deleted", async () => {
    vi.spyOn(window, "confirm").mockReturnValue(true);
    vi.mocked(deleteCategory).mockRejectedValue(
      new ApiError(409, "Food still has 3 transactions. Move or delete them first."),
    );
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Food");

    await user.click(screen.getByRole("button", { name: "Delete Food" }));

    expect(window.confirm).toHaveBeenCalledWith("Delete Food?");
    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Food still has 3 transactions. Move or delete them first.",
    );
  });
});
