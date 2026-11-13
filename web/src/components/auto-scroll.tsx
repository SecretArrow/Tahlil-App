"use client";

import * as React from "react";
import { motion } from "framer-motion";
import { Pause, Play } from "lucide-react";
import { tahlilSections } from "@/data/tahlil-content";
import { useDisplaySettings } from "@/components/display-settings-provider";

/**
 * AutoScroll — "Gulir Otomatis" controller for the reading page.
 *
 * Behaviour (per user request):
 *  - A bottom-left FAB with a start/stop icon. Tap it → auto-scroll starts
 *    (icon flips to Pause); tap again → it stops (icon flips back to Play).
 *  - While scrolling, ANY tap (or long-press, which ends as a click on
 *    touch devices) on the page PAUSES the scroll; tapping again RESUMES.
 *    Taps on interactive elements (links, buttons, inputs, dialogs, the
 *    FABs themselves) are ignored so UI still works normally.
 *  - Manual wheel / touch-scroll while running also pauses — the user has
 *    taken over.
 *  - Optional "Jeda per Bagian": when a new section card scrolls into view
 *    the engine holds for N seconds, then continues automatically.
 *  - Reaching the bottom stops the feature.
 *
 * Speed & section-pause duration come from the display settings
 * (autoScrollSpeed, autoScrollSectionPauseSec) configured in the
 * "Tampilan Bacaan" panel → "Gulir Otomatis" section.
 */

const BASE_SPEED_PX_PER_SEC = 55;

/** Selectors whose clicks must NOT toggle pause. */
const INTERACTIVE_SELECTOR =
  'a, button, input, select, textarea, label, summary, [role="button"], [role="dialog"], dialog, [data-no-autoscroll-pause]';

export function AutoScroll() {
  const { toggles } = useDisplaySettings();
  const speed = toggles.autoScrollSpeed;
  const sectionPauseSec = toggles.autoScrollSectionPauseSec;

  // "active" = feature on; "paused" = on hold by tap / wheel / section hold.
  // Both are session-only (not persisted).
  const [active, setActive] = React.useState(false);
  const [paused, setPaused] = React.useState(false);
  const scrolling = active && !paused;

  // Refs so the rAF loop & listeners read the latest values without
  // restarting the effect on every state change.
  const scrollingRef = React.useRef(scrolling);
  scrollingRef.current = scrolling;
  const speedRef = React.useRef(speed);
  speedRef.current = speed;
  const sectionPauseSecRef = React.useRef(sectionPauseSec);
  sectionPauseSecRef.current = sectionPauseSec;
  const holdUntilRef = React.useRef(0);
  const lastSectionRef = React.useRef<string>("");

  const stop = React.useCallback(() => {
    setActive(false);
    setPaused(false);
    holdUntilRef.current = 0;
    lastSectionRef.current = "";
  }, []);

  const togglePause = React.useCallback(() => {
    setPaused((p) => !p);
    holdUntilRef.current = 0;
  }, []);

  const toggleActive = React.useCallback(() => {
    setActive((a) => {
      if (a) {
        setPaused(false);
        holdUntilRef.current = 0;
        lastSectionRef.current = "";
        return false;
      }
      setPaused(false);
      return true;
    });
  }, []);

  // --- rAF scroll engine --------------------------------------------------
  React.useEffect(() => {
    if (!scrolling) return;
    let raf = 0;
    let last: number | null = null;
    const step = (now: number) => {
      raf = requestAnimationFrame(step);
      if (last === null) {
        last = now;
        return;
      }
      const dt = (now - last) / 1000;
      last = now;
      // Section hold — wait out the configured pause before continuing.
      if (performance.now() < holdUntilRef.current) return;
      window.scrollBy(0, BASE_SPEED_PX_PER_SEC * speedRef.current * dt);
      // Bottom reached → stop the feature entirely.
      const doc = document.documentElement;
      if (window.innerHeight + window.scrollY >= doc.scrollHeight - 2) {
        setActive(false);
        setPaused(false);
      }
    };
    raf = requestAnimationFrame(step);
    return () => cancelAnimationFrame(raf);
  }, [scrolling]);

  // --- Section-pause observer ---------------------------------------------
  // Mirrors the header's active-section tracker: a card counts as "entered"
  // when its top crosses the upper third of the viewport. While running,
  // each NEW section holds the scroll for N seconds.
  React.useEffect(() => {
    if (!scrolling || sectionPauseSec <= 0) return;
    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (!entry.isIntersecting) continue;
          const id = entry.target.id;
          if (id && id !== lastSectionRef.current) {
            lastSectionRef.current = id;
            holdUntilRef.current = performance.now() + sectionPauseSecRef.current * 1000;
          }
        }
      },
      { rootMargin: "-30% 0px -55% 0px", threshold: 0 },
    );
    for (const s of tahlilSections) {
      const el = document.getElementById(s.id);
      if (el) observer.observe(el);
    }
    return () => observer.disconnect();
  }, [scrolling, sectionPauseSec]);

  // --- Tap-anywhere pause/resume -------------------------------------------
  React.useEffect(() => {
    if (!active) return;
    const onClick = (e: MouseEvent) => {
      const target = e.target as HTMLElement | null;
      if (!target) return;
      if (target.closest(INTERACTIVE_SELECTOR)) return;
      togglePause();
    };
    document.addEventListener("click", onClick);
    return () => document.removeEventListener("click", onClick);
  }, [active, togglePause]);

  // --- Manual wheel / touch scroll pauses ----------------------------------
  // The engine's programmatic window.scrollBy does NOT emit wheel/touchmove
  // events, so these only fire from real user input — any manual scroll
  // gesture takes over and pauses the auto-scroll.
  React.useEffect(() => {
    if (!scrolling) return;
    const pause = () => {
      setPaused(true);
      holdUntilRef.current = 0;
    };
    window.addEventListener("wheel", pause, { passive: true });
    window.addEventListener("touchmove", pause, { passive: true });
    return () => {
      window.removeEventListener("wheel", pause);
      window.removeEventListener("touchmove", pause);
    };
  }, [scrolling]);

  return (
    <div className="fixed bottom-5 left-5 z-40 sm:bottom-6 sm:left-6">
      {/* Paused hint pill */}
      {active && paused && (
        <motion.div
          initial={{ opacity: 0, y: 6 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: 6 }}
          className="mb-2 inline-block rounded-full bg-foreground/85 px-3 py-1 text-[11px] font-medium text-background shadow"
        >
          Gulir dijeda — ketuk layar untuk lanjut
        </motion.div>
      )}
      <motion.button
        type="button"
        data-no-autoscroll-pause
        onClick={toggleActive}
        aria-label={active ? "Hentikan gulir otomatis" : "Mulai gulir otomatis"}
        title={
          active
            ? "Hentikan gulir otomatis"
            : "Mulai gulir otomatis (ketuk layar untuk jeda/lanjut)"
        }
        initial={false}
        whileHover={{ scale: 1.08 }}
        whileTap={{ scale: 0.92 }}
        className={`flex h-12 w-12 items-center justify-center rounded-full shadow-lg transition-colors ${
          active
            ? "bg-gradient-to-br from-emerald-600 to-teal-700 text-white shadow-emerald-900/30 dark:from-emerald-500 dark:to-teal-600"
            : "border border-border bg-card text-foreground"
        }`}
      >
        {scrolling ? (
          <Pause className="h-5 w-5" strokeWidth={2.5} />
        ) : (
          <Play className="h-5 w-5" strokeWidth={2.5} />
        )}
      </motion.button>
    </div>
  );
}
