import { createBrowserRouter, Navigate } from "react-router";
import { RequireAuth } from "./auth/RequireAuth";
import { Layout } from "./components/Layout";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { TransactionsPage } from "./pages/TransactionsPage";

export const router = createBrowserRouter([
  { path: "/login", element: <LoginPage /> },
  { path: "/register", element: <RegisterPage /> },
  {
    element: <RequireAuth />,
    children: [
      {
        path: "/",
        element: <Layout />,
        children: [{ index: true, element: <TransactionsPage /> }],
      },
    ],
  },
  { path: "*", element: <Navigate to="/" replace /> },
]);
