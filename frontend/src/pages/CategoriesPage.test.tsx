import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { createCategory, deleteCategory, fetchCategories, renameCategory } from "../api/categories";
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
      { id: 1, name: "Food" },
      { id: 2, name: "Transport" },
    ]);
  });

  it("adds a category and clears the field", async () => {
    vi.mocked(createCategory).mockResolvedValue({ id: 3, name: "Coffee" });
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Food");

    await user.type(screen.getByLabelText("New category"), "Coffee");
    await user.click(screen.getByRole("button", { name: "Add category" }));

    expect(createCategory).toHaveBeenCalledWith("Coffee");
    await waitFor(() => expect(screen.getByLabelText("New category")).toHaveValue(""));
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
    vi.mocked(renameCategory).mockResolvedValue({ id: 1, name: "Groceries" });
    const user = userEvent.setup();
    renderPage();
    await screen.findByText("Food");

    await user.click(screen.getByRole("button", { name: "Rename Food" }));
    const field = screen.getByLabelText("Rename Food");
    expect(field).toHaveValue("Food");
    await user.clear(field);
    await user.type(field, "Groceries");
    await user.click(screen.getByRole("button", { name: "Save" }));

    expect(renameCategory).toHaveBeenCalledWith(1, "Groceries");
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
