import { useQueryClient } from "@tanstack/react-query";
import { type ReactNode, useCallback, useEffect, useMemo, useState } from "react";
import { getToken, setToken, setUnauthorizedHandler } from "../api/client";
import { AuthContext } from "./AuthContext";

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [token, setTokenState] = useState<string | null>(() => getToken());

  const logIn = useCallback((newToken: string) => {
    setToken(newToken);
    setTokenState(newToken);
  }, []);

  const logOut = useCallback(() => {
    setToken(null);
    setTokenState(null);
    // Forget everything cached for this user, so the next person to log in can't see it.
    queryClient.clear();
  }, [queryClient]);

  // If the backend ever rejects the token (for example, it expired), log out.
  useEffect(() => {
    setUnauthorizedHandler(logOut);
    return () => setUnauthorizedHandler(() => {});
  }, [logOut]);

  const value = useMemo(() => ({ token, logIn, logOut }), [token, logIn, logOut]);
  return <AuthContext value={value}>{children}</AuthContext>;
}
