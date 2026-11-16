import { useState } from "react";

const KEY = "expense-tracker.show-income";

/** Whether the overview shows money in. It's a display choice, so this browser remembers it. */
export function useShowIncome(): [boolean, (show: boolean) => void] {
  const [show, setShow] = useState(() => localStorage.getItem(KEY) !== "no");

  function update(next: boolean) {
    setShow(next);
    if (next) {
      localStorage.removeItem(KEY);
    } else {
      localStorage.setItem(KEY, "no");
    }
  }

  return [show, update];
}
