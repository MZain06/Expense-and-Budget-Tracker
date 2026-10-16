import { Navigate, Outlet, useLocation } from "react-router";
import { useAuth } from "./useAuth";

/** Shows its child routes only to logged-in users; everyone else goes to the login page. */
export function RequireAuth() {
  const { token } = useAuth();
  const location = useLocation();

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  return <Outlet />;
}
