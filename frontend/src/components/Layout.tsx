import { useQuery } from "@tanstack/react-query";
import { Link, NavLink, Outlet } from "react-router";
import { fetchMe } from "../api/auth";
import { useAuth } from "../auth/useAuth";
import { BrandMark } from "./BrandMark";
import { Button } from "./Button";

const sections = [
  { to: "/transactions", label: "Transactions" },
  { to: "/budgets", label: "Budgets" },
  { to: "/categories", label: "Categories" },
];

export function Layout() {
  const { logOut } = useAuth();
  const { data: me } = useQuery({ queryKey: ["me"], queryFn: fetchMe });

  return (
    <div className="min-h-dvh">
      <header className="border-b border-line bg-surface">
        <div className="mx-auto max-w-3xl px-4">
          <div className="flex items-center justify-between gap-4 py-3">
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
          <nav aria-label="Main" className="-mb-px flex gap-6 overflow-x-auto text-sm">
            {sections.map((section) => (
              <NavLink
                key={section.to}
                to={section.to}
                end
                className={({ isActive }) =>
                  `border-b-2 py-2.5 whitespace-nowrap ${
                    isActive
                      ? "border-brand font-medium text-ink"
                      : "border-transparent text-muted hover:text-ink"
                  }`
                }
              >
                {section.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-3xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
}
