import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { login } from "../api/auth";
import { ApiError, getToken } from "../api/client";
import { renderRoutes } from "../test/renderRoutes";
import { LoginPage } from "./LoginPage";

vi.mock("../api/auth");

const routes = [
  { path: "/login", element: <LoginPage /> },
  { path: "/", element: <p>Home page</p> },
];

describe("LoginPage", () => {
  it("logs in, saves the token and goes to the home page", async () => {
    vi.mocked(login).mockResolvedValue({
      accessToken: "token-123",
      tokenType: "Bearer",
      expiresIn: 3600,
    });
    const user = userEvent.setup();
    renderRoutes(routes, "/login");

    await user.type(screen.getByLabelText("Email"), "zain@example.com");
    await user.type(screen.getByLabelText("Password"), "correct-horse-battery");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    expect(await screen.findByText("Home page")).toBeInTheDocument();
    expect(login).toHaveBeenCalledWith("zain@example.com", "correct-horse-battery");
    expect(getToken()).toBe("token-123");
  });

  it("shows the server's message when the details are wrong", async () => {
    vi.mocked(login).mockRejectedValue(new ApiError(401, "Invalid email or password"));
    const user = userEvent.setup();
    renderRoutes(routes, "/login");

    await user.type(screen.getByLabelText("Email"), "zain@example.com");
    await user.type(screen.getByLabelText("Password"), "wrong-password");
    await user.click(screen.getByRole("button", { name: "Log in" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("Invalid email or password");
    expect(getToken()).toBeNull();
  });

  it("checks both fields before contacting the server", async () => {
    const user = userEvent.setup();
    renderRoutes(routes, "/login");

    await user.click(screen.getByRole("button", { name: "Log in" }));

    expect(await screen.findByText("Enter a valid email address")).toBeInTheDocument();
    expect(screen.getByText("Enter your password")).toBeInTheDocument();
    expect(login).not.toHaveBeenCalled();
  });
});
