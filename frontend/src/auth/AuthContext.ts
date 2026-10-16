import { createContext } from "react";

export type AuthState = {
  token: string | null;
  logIn: (token: string) => void;
  logOut: () => void;
};

export const AuthContext = createContext<AuthState | null>(null);
