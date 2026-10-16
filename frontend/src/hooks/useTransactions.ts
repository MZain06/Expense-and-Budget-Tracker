import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  createTransaction,
  deleteTransaction,
  fetchTransactions,
  updateTransaction,
} from "../api/transactions";
import type { TransactionInput } from "../types/api";

export function useTransactions(month: string, categoryId: number | null) {
  return useQuery({
    queryKey: ["transactions", month, categoryId],
    queryFn: () => fetchTransactions(month, categoryId),
  });
}

/** Creates a transaction, or updates one when an id is given, then refreshes every list. */
export function useSaveTransaction() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, input }: { id?: number; input: TransactionInput }) =>
      id === undefined ? createTransaction(input) : updateTransaction(id, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["transactions"] }),
  });
}

export function useDeleteTransaction() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => deleteTransaction(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["transactions"] }),
  });
}
