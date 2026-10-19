import { useQuery } from "@tanstack/react-query";
import { fetchInsights } from "../api/insights";
import { fetchSummary } from "../api/summary";

export function useSummary(month: string) {
  return useQuery({
    queryKey: ["summary", month],
    queryFn: () => fetchSummary(month),
  });
}

export function useInsights(month: string) {
  return useQuery({
    queryKey: ["insights", month],
    queryFn: () => fetchInsights(month),
  });
}
