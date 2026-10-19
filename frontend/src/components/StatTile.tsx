import type { ReactNode } from "react";

type StatTileProps = {
  label: string;
  value: string;
  detail?: ReactNode;
};

/** One headline number with a short label and an optional line of context. */
export function StatTile({ label, value, detail }: StatTileProps) {
  return (
    <div className="rounded-lg border border-line bg-surface p-4">
      <p className="text-sm text-muted">{label}</p>
      <p className="mt-1 text-2xl font-semibold tracking-tight">{value}</p>
      {detail && <p className="mt-1 text-sm text-muted">{detail}</p>}
    </div>
  );
}
