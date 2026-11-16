import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createCategory, deleteCategory, fetchCategories, updateCategory } from "../api/categories";
import type { CategoryKind } from "../types/api";

export function useCategories() {
  return useQuery({
    queryKey: ["categories"],
    queryFn: fetchCategories,
    staleTime: 5 * 60 * 1000,
  });
}

type CategoryValues = { name: string; kind: CategoryKind };

// Category names show up on every screen, so any change to a category refreshes everything.
function useRefreshAll() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries();
}

export function useCreateCategory() {
  const refreshAll = useRefreshAll();
  return useMutation({
    mutationFn: ({ name, kind }: CategoryValues) => createCategory(name, kind),
    onSuccess: refreshAll,
  });
}

export function useUpdateCategory() {
  const refreshAll = useRefreshAll();
  return useMutation({
    mutationFn: ({ id, name, kind }: CategoryValues & { id: number }) =>
      updateCategory(id, name, kind),
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
