import type { Transaction, TransactionInput } from "../types/api";
import { apiFetch } from "./client";

export function fetchTransactions(
  month: string,
  categoryId: number | null,
): Promise<Transaction[]> {
  const params = new URLSearchParams({ month });
  if (categoryId !== null) {
    params.set("categoryId", String(categoryId));
  }
  return apiFetch<Transaction[]>(`/transactions?${params}`);
}

export function createTransaction(input: TransactionInput): Promise<Transaction> {
  return apiFetch<Transaction>("/transactions", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function updateTransaction(id: number, input: TransactionInput): Promise<Transaction> {
  return apiFetch<Transaction>(`/transactions/${id}`, {
    method: "PUT",
    body: JSON.stringify(input),
  });
}

export function deleteTransaction(id: number): Promise<void> {
  return apiFetch<void>(`/transactions/${id}`, { method: "DELETE" });
}
