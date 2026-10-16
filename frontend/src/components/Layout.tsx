import { useQuery } from "@tanstack/react-query";
import { Link, Outlet } from "react-router";
import { fetchMe } from "../api/auth";
import { useAuth } from "../auth/useAuth";
import { BrandMark } from "./BrandMark";
import { Button } from "./Button";

export function Layout() {
  const { logOut } = useAuth();
  const { data: me } = useQuery({ queryKey: ["me"], queryFn: fetchMe });

  return (
    <div className="min-h-dvh">
      <header className="border-b border-line bg-surface">
        <div className="mx-auto flex max-w-3xl items-center justify-between gap-4 px-4 py-3">
          <Link to="/" className="flex items-center gap-2 font-semibold">
            <BrandMark />
            Expense Tracker
          </Link>
          <div className="flex items-center gap-3 text-sm">
            {me && <span className="hidden text-muted sm:inline">{me.email}</span>}
            <Button variant="secondary" onClick={logOut}>
              Log out
            </Button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-3xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
}
