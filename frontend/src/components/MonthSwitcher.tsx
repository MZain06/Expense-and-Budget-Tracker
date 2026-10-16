import { formatMonth, shiftMonth } from "../lib/dates";
import { Button } from "./Button";
import { ChevronLeftIcon, ChevronRightIcon } from "./icons";

type MonthSwitcherProps = {
  month: string;
  onChange: (month: string) => void;
};

export function MonthSwitcher({ month, onChange }: MonthSwitcherProps) {
  return (
    <div className="flex items-center justify-between gap-4">
      <h1 className="text-3xl font-semibold tracking-tight">{formatMonth(month)}</h1>
      <div className="flex gap-2">
        <Button
          variant="secondary"
          size="icon"
          aria-label="Previous month"
          onClick={() => onChange(shiftMonth(month, -1))}
        >
          <ChevronLeftIcon />
        </Button>
        <Button
          variant="secondary"
          size="icon"
          aria-label="Next month"
          onClick={() => onChange(shiftMonth(month, 1))}
        >
          <ChevronRightIcon />
        </Button>
      </div>
    </div>
  );
}
