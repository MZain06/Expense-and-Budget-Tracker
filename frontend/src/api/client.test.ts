import { afterEach, describe, expect, it, vi } from "vitest";
import { ApiError, apiFetch, setToken, setUnauthorizedHandler } from "./client";

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

describe("apiFetch", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    setUnauthorizedHandler(() => {});
  });

  it("sends the saved token and returns the parsed JSON", async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(200, { id: 1 }));
    vi.stubGlobal("fetch", fetchMock);
    setToken("abc123");

    await expect(apiFetch("/me")).resolves.toEqual({ id: 1 });

    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe("/api/me");
    expect(new Headers(init.headers).get("Authorization")).toBe("Bearer abc123");
  });

  it("turns a problem-details response into an ApiError with field errors", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        jsonResponse(400, {
          detail: "Request validation failed",
          errors: { amount: "must be greater than 0" },
        }),
      ),
    );

    const error = await apiFetch("/transactions", { method: "POST", body: "{}" }).catch(
      (e: unknown) => e,
    );

    expect(error).toBeInstanceOf(ApiError);
    expect(error).toMatchObject({
      status: 400,
      message: "Request validation failed",
      fieldErrors: { amount: "must be greater than 0" },
    });
  });

  it("calls the unauthorized handler when a logged-in request gets a 401", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(jsonResponse(401, {})));
    const handler = vi.fn();
    setUnauthorizedHandler(handler);
    setToken("expired-token");

    await expect(apiFetch("/me")).rejects.toBeInstanceOf(ApiError);
    expect(handler).toHaveBeenCalledOnce();
  });

  it("keeps the token off login and registration requests", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValue(jsonResponse(401, { detail: "Invalid email or password" }));
    vi.stubGlobal("fetch", fetchMock);
    const handler = vi.fn();
    setUnauthorizedHandler(handler);
    setToken("expired-token");

    await expect(apiFetch("/auth/login", { method: "POST", body: "{}" })).rejects.toThrow(
      "Invalid email or password",
    );

    const [, init] = fetchMock.mock.calls[0];
    expect(new Headers(init.headers).has("Authorization")).toBe(false);
    expect(handler).not.toHaveBeenCalled();
  });

  it("explains when the backend can't be reached", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response("", { status: 502 })));

    await expect(apiFetch("/me")).rejects.toThrow("Couldn't reach the server. Try again.");
  });

  it("returns nothing for 204 No Content", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response(null, { status: 204 })));

    await expect(apiFetch("/transactions/1", { method: "DELETE" })).resolves.toBeUndefined();
  });
});
