import type { Category } from "../types/api";
import { apiFetch } from "./client";

export function fetchCategories(): Promise<Category[]> {
  return apiFetch<Category[]>("/categories");
}
