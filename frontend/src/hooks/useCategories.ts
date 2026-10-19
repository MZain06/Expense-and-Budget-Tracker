import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createCategory, deleteCategory, fetchCategories, renameCategory } from "../api/categories";

export function useCategories() {
  return useQuery({
    queryKey: ["categories"],
    queryFn: fetchCategories,
    staleTime: 5 * 60 * 1000,
  });
}

// Category names show up on every screen, so any change to a category refreshes everything.
function useRefreshAll() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries();
}

export function useCreateCategory() {
  const refreshAll = useRefreshAll();
  return useMutation({
    mutationFn: (name: string) => createCategory(name),
    onSuccess: refreshAll,
  });
}

export function useRenameCategory() {
  const refreshAll = useRefreshAll();
  return useMutation({
    mutationFn: ({ id, name }: { id: number; name: string }) => renameCategory(id, name),
    onSuccess: refreshAll,
  });
}

export function useDeleteCategory() {
  const refreshAll = useRefreshAll();
  return useMutation({
    mutationFn: (id: number) => deleteCategory(id),
    onSuccess: refreshAll,
  });
}
