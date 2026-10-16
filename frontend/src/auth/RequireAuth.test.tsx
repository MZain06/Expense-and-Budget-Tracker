import { screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { setToken } from "../api/client";
import { renderRoutes } from "../test/renderRoutes";
import { RequireAuth } from "./RequireAuth";

const routes = [
  { path: "/login", element: <p>Login page</p> },
  {
    element: <RequireAuth />,
    children: [{ path: "/", element: <p>Private page</p> }],
  },
];

describe("RequireAuth", () => {
  it("sends visitors without a token to the login page", async () => {
    renderRoutes(routes, "/");

    expect(await screen.findByText("Login page")).toBeInTheDocument();
    expect(screen.queryByText("Private page")).not.toBeInTheDocument();
  });

  it("shows the page to logged-in users", async () => {
    setToken("a-valid-token");
    renderRoutes(routes, "/");

    expect(await screen.findByText("Private page")).toBeInTheDocument();
  });
});
