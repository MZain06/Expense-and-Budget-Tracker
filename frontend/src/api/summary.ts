import type { MonthlySummary } from "../types/api";
import { apiFetch } from "./client";

export function fetchSummary(month: string): Promise<MonthlySummary> {
  return apiFetch<MonthlySummary>(`/summary?${new URLSearchParams({ month })}`);
}
