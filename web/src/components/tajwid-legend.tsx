"use client";

import { TAJWID_RULES_META } from "@/lib/tajwid";

/**
 * TajwidLegend — color swatch + label list for the 7 tajwid rules
 * highlighted by the `tajwid` display toggle. Shown in the settings
 * panels whenever the toggle is active so users can decode the colors.
 *
 * Colors come from the `.tajwid-<rule>` classes in globals.css, so the
 * legend always matches the actual highlighting (light & dark theme).
 */
export function TajwidLegend({ className = "" }: { className?: string }) {
  return (
    <div className={`grid grid-cols-1 gap-x-4 gap-y-1.5 sm:grid-cols-2 ${className}`}>
      {TAJWID_RULES_META.map(({ rule, name, desc }) => (
        <div key={rule} className="flex min-w-0 items-center gap-2">
          <span
            aria-hidden="true"
            className={`tajwid-${rule} h-2.5 w-2.5 shrink-0 rounded-full bg-current`}
          />
          <span className="min-w-0 text-xs leading-snug text-muted-foreground">
            <span className={`tajwid-${rule} font-semibold`}>{name}</span>
            {" — "}
            {desc}
          </span>
        </div>
      ))}
    </div>
  );
}
