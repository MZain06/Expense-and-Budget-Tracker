import type { Budget } from "../types/api";
import { apiFetch } from "./client";

export function fetchBudgets(month: string): Promise<Budget[]> {
  return apiFetch<Budget[]>(`/budgets?${new URLSearchParams({ month })}`);
}

export function setBudget(month: string, categoryId: number, limit: number): Promise<Budget> {
  return apiFetch<Budget>(`/budgets/${month}/${categoryId}`, {
    method: "PUT",
    body: JSON.stringify({ limit }),
  });
}

export function removeBudget(month: string, categoryId: number): Promise<void> {
  return apiFetch<void>(`/budgets/${month}/${categoryId}`, { method: "DELETE" });
}

export function copyBudgetsFromPreviousMonth(month: string): Promise<Budget[]> {
  return apiFetch<Budget[]>(`/budgets/${month}/copy-from-previous`, { method: "POST" });
}
