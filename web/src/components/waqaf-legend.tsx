"use client";

import { WAQAF_RULES_META } from "@/lib/waqaf";

/**
 * WaqafLegend — blue swatch + explanation for the waqaf coloring feature
 * (separate from the 7-color tajwid legend). Shown in the settings panels
 * whenever the `waqafColor` toggle is active.
 *
 * The blue marks letters whose reading changes when the reciter stops:
 * waqaf bil iskan, mad aridh lissukun, mad iwadh. Light theme uses the
 * exact #1B51D0; dark theme lightens the same blue (#7B9DFF) for
 * contrast — the legend swatch follows the active theme via the same
 * `.waqaf-mark` class the text uses.
 */
export function WaqafLegend({ className = "" }: { className?: string }) {
  return (
    <div className={`space-y-1.5 ${className}`}>
      {WAQAF_RULES_META.map(({ kind, name, desc }) => (
        <div key={kind} className="flex min-w-0 items-center gap-2">
          <span
            aria-hidden="true"
            className="waqaf-mark h-2.5 w-2.5 shrink-0 rounded-full bg-current"
          />
          <span className="min-w-0 text-xs leading-snug text-muted-foreground">
            <span className="waqaf-mark font-semibold">{name}</span>
            {" — "}
            {desc}
          </span>
        </div>
      ))}
      <p className="text-[11px] leading-snug text-muted-foreground/80">
        Warna biru <span className="waqaf-mark font-semibold">#1B51D0</span> menandai huruf yang
        terpengaruh cara baca ketika berhenti (di akhir klausa, akhir ayat, dan akhir teks). Teks
        asli Al-Qur&apos;an tidak diubah.
      </p>
    </div>
  );
}
