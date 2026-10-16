const TOKEN_KEY = "expense-tracker.token";

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

let onUnauthorized: () => void = () => {};

/** Lets the app react (by logging out) when the backend rejects the saved token. */
export function setUnauthorizedHandler(handler: () => void): void {
  onUnauthorized = handler;
}

/** An error response from the backend, read from its problem-details JSON. */
export class ApiError extends Error {
  readonly status: number;
  readonly fieldErrors: Record<string, string>;

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

/** Calls the backend at /api{path}, adding the saved token and turning errors into ApiError. */
export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  // Logging in and registering don't need the token, and the backend turns away any request
  // that carries an expired one, so it only goes to the other endpoints.
  const token = path.startsWith("/auth/") ? null : getToken();
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  if (options.body !== undefined && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`/api${path}`, { ...options, headers });

  if (response.status === 401 && token) {
    onUnauthorized();
  }
  if (!response.ok) {
    throw await toApiError(response);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

async function toApiError(response: Response): Promise<ApiError> {
  // 502, 503 and 504 come from the proxy in front of the backend when the backend isn't answering.
  const fallback =
    response.status >= 502 && response.status <= 504
      ? "Couldn't reach the server. Try again."
      : `Request failed (${response.status})`;
  try {
    const problem = (await response.json()) as {
      detail?: string;
      errors?: Record<string, string>;
    };
    return new ApiError(response.status, problem.detail ?? fallback, problem.errors ?? {});
  } catch {
    return new ApiError(response.status, fallback);
  }
}
