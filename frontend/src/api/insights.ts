import type { Insights } from "../types/api";
import { apiFetch } from "./client";

export function fetchInsights(month: string): Promise<Insights> {
  return apiFetch<Insights>(`/insights?${new URLSearchParams({ month })}`);
}
