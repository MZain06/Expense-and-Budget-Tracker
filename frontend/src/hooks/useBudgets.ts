import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  copyBudgetsFromPreviousMonth,
  fetchBudgets,
  removeBudget,
  setBudget,
} from "../api/budgets";

export function useBudgets(month: string) {
  return useQuery({
    queryKey: ["budgets", month],
    queryFn: () => fetchBudgets(month),
  });
}

/** One budget to save: a number sets the limit, null removes the budget. */
export type BudgetChange = { categoryId: number; limit: number | null };

// The overview shows budgets too, so its summary is refreshed along with the budget lists.
function useRefreshBudgets() {
  const queryClient = useQueryClient();
  return () =>
    Promise.all([
      queryClient.invalidateQueries({ queryKey: ["budgets"] }),
      queryClient.invalidateQueries({ queryKey: ["summary"] }),
    ]);
}

/** Saves several budgets at once. */
export function useSaveBudgets(month: string) {
  const refreshBudgets = useRefreshBudgets();
  return useMutation({
    mutationFn: (changes: BudgetChange[]) =>
      Promise.all(
        changes.map(({ categoryId, limit }) =>
          limit === null ? removeBudget(month, categoryId) : setBudget(month, categoryId, limit),
        ),
      ),
    // Refresh even if something failed, because the other changes may have saved.
    onSettled: refreshBudgets,
  });
}

export function useCopyBudgets(month: string) {
  const refreshBudgets = useRefreshBudgets();
  return useMutation({
    mutationFn: () => copyBudgetsFromPreviousMonth(month),
    onSettled: refreshBudgets,
  });
}
