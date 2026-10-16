import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { createAccount, login } from "../api/auth";
import { ApiError } from "../api/client";
import { renderRoutes } from "../test/renderRoutes";
import { RegisterPage } from "./RegisterPage";

vi.mock("../api/auth");

const routes = [
  { path: "/register", element: <RegisterPage /> },
  { path: "/", element: <p>Home page</p> },
];

async function fillInForm(password: string, confirmPassword: string) {
  const user = userEvent.setup();
  await user.type(screen.getByLabelText("Email"), "zain@example.com");
  await user.type(screen.getByLabelText("Password"), password);
  await user.type(screen.getByLabelText("Confirm password"), confirmPassword);
  await user.click(screen.getByRole("button", { name: "Create account" }));
}

describe("RegisterPage", () => {
  it("creates the account, logs in and goes to the home page", async () => {
    vi.mocked(createAccount).mockResolvedValue({ id: 1, email: "zain@example.com" });
    vi.mocked(login).mockResolvedValue({
      accessToken: "token-123",
      tokenType: "Bearer",
      expiresIn: 3600,
    });
    renderRoutes(routes, "/register");

    await fillInForm("correct-horse-battery", "correct-horse-battery");

    expect(await screen.findByText("Home page")).toBeInTheDocument();
    expect(createAccount).toHaveBeenCalledWith("zain@example.com", "correct-horse-battery");
  });

  it("stops when the passwords don't match", async () => {
    renderRoutes(routes, "/register");

    await fillInForm("correct-horse-battery", "something-else");

    expect(await screen.findByText("Passwords don't match")).toBeInTheDocument();
    expect(createAccount).not.toHaveBeenCalled();
  });

  it("shows the server's message for an email that is already registered", async () => {
    vi.mocked(createAccount).mockRejectedValue(
      new ApiError(409, "An account with this email already exists"),
    );
    renderRoutes(routes, "/register");

    await fillInForm("correct-horse-battery", "correct-horse-battery");

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "An account with this email already exists",
    );
    expect(login).not.toHaveBeenCalled();
  });
});
