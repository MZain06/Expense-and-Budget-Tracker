import type { ReactNode } from "react";
import { BrandMark } from "./BrandMark";

type AuthLayoutProps = {
  title: string;
  subtitle: string;
  children: ReactNode;
};

/** The narrow, centred column used by the login and registration pages. */
export function AuthLayout({ title, subtitle, children }: AuthLayoutProps) {
  return (
    <div className="flex min-h-dvh items-center justify-center px-4 py-12">
      <div className="w-full max-w-sm">
        <div className="flex items-center gap-2 font-semibold">
          <BrandMark />
          Expense Tracker
        </div>
        <h1 className="mt-8 text-3xl font-semibold tracking-tight">{title}</h1>
        <p className="mt-2 text-muted">{subtitle}</p>
        <div className="mt-6 rounded-lg border border-line bg-surface p-6">{children}</div>
      </div>
    </div>
  );
}
