"use client";

import * as React from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Pause, Play, FastForward, ArrowDownToLine } from "lucide-react";
import { tahlilSections } from "@/data/tahlil-content";
import { useDisplaySettings } from "@/components/display-settings-provider";

/**
 * AutoScroll — "Gulir Otomatis" controller for the reading page.
 *
 * Behaviour (per user spec):
 *  - When `toggles.autoScrollEnabled` is false, the FAB is NOT rendered at
 *    all ("bisa enable disable auto scroll, jika di enable maka icon start
 *    pause-nya muncul di layar").
 *  - FAB tap toggles running ⟷ paused. Tap or long-press anywhere on the
 *    reading surface = pause (when running) / resume (when paused).
 *  - Two scroll modes:
 *      * "continuous": smooth pixel-by-pixel scrolling at a fixed rate
 *        derived from `autoScrollSpeed` (px/sec = 12 + speed * 18).
 *      * "block":      jumps to the next `[data-autoscroll-block]` element
 *        with a delay derived from `autoScrollSpeed`
 *        (delay = clamp(7000 / speed, 700, 30000) ms).
 *  - The "Jeda per Bagian" setting (autoScrollSectionPauseSec) only applies
 *    to continuous mode — it holds the scroll for N seconds whenever a new
 *    section card scrolls into view.
 *  - Reaching the bottom stops the feature.
 *
 * Size & position (per user spec):
 *  - The FAB is sized h-9 w-9 (36px) — 70% of the existing back-to-top
 *    button (h-12 w-12 = 48px) ("kecilkan ukurannya 70% dari ukuran
 *    sekarang").
 *  - Pinned to the RIGHT edge ("posisinya pindah ke kanan"), above the
 *    existing back-to-top button so the two never overlap.
 */

// Continuous mode: px/sec = BASE + speed * FACTOR.
// (Matches the Android engine so the pace feels identical across platforms.)
const BASE_SPEED_PX_PER_SEC = 12;
const SPEED_FACTOR_PX_PER_SEC = 18;

// Block mode: delayMs = clamp(BASE_MS / speed, MIN_MS, MAX_MS).
const BLOCK_BASE_MS = 7000;
const BLOCK_MIN_MS = 700;
const BLOCK_MAX_MS = 30000;

/** Selectors whose clicks must NOT toggle pause. */
const INTERACTIVE_SELECTOR =
  'a, button, input, select, textarea, label, summary, [role="button"], [role="dialog"], dialog, [data-no-autoscroll-pause]';

export function AutoScroll() {
  const { toggles } = useDisplaySettings();
  const enabled = toggles.autoScrollEnabled;
  const mode = toggles.autoScrollMode;
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
  const modeRef = React.useRef(mode);
  modeRef.current = mode;
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

  // If the user disables the feature while it's running, stop.
  React.useEffect(() => {
    if (!enabled && active) stop();
  }, [enabled, active, stop]);

  // If the mode changes while running, reset section-tracking so the new
  // engine starts fresh.
  React.useEffect(() => {
    lastSectionRef.current = "";
    holdUntilRef.current = 0;
  }, [mode]);

  // --- Continuous mode: rAF loop ------------------------------------------
  React.useEffect(() => {
    if (!scrolling || mode !== "continuous") return;
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
      const pxPerSec = BASE_SPEED_PX_PER_SEC + speedRef.current * SPEED_FACTOR_PX_PER_SEC;
      window.scrollBy(0, pxPerSec * dt);
      // Bottom reached → stop the feature entirely.
      const doc = document.documentElement;
      if (window.innerHeight + window.scrollY >= doc.scrollHeight - 2) {
        setActive(false);
        setPaused(false);
      }
    };
    raf = requestAnimationFrame(step);
    return () => cancelAnimationFrame(raf);
  }, [scrolling, mode]);

  // --- Block mode: timer jumps to next [data-autoscroll-block] -------------
  React.useEffect(() => {
    if (!scrolling || mode !== "block") return;
    let timer: number | null = null;
    const tick = () => {
      if (paused) return;
      const blocks = Array.from(document.querySelectorAll<HTMLElement>("[data-autoscroll-block]"));
      if (blocks.length === 0) {
        setActive(false);
        return;
      }
      const viewportTop = window.scrollY + 80;
      let nextIdx = blocks.findIndex((b) => b.offsetTop >= viewportTop);
      if (nextIdx === -1) {
        // All blocks are above — restart from top.
        nextIdx = 0;
        window.scrollTo({ top: 0, behavior: "smooth" });
      }
      const target = blocks[nextIdx];
      if (target) {
        target.scrollIntoView({ behavior: "smooth", block: "start" });
      }
      // Reached the last block — wait one final delay, then stop.
      if (nextIdx >= blocks.length - 1) {
        timer = window.setTimeout(
          () => {
            if (!paused) {
              setActive(false);
            }
          },
          Math.max(BLOCK_MIN_MS, Math.min(BLOCK_MAX_MS, BLOCK_BASE_MS / speedRef.current)),
        );
        return;
      }
      timer = window.setTimeout(
        tick,
        Math.max(BLOCK_MIN_MS, Math.min(BLOCK_MAX_MS, BLOCK_BASE_MS / speedRef.current)),
      );
    };
    // Initial delay before the first jump so the user can settle.
    timer = window.setTimeout(tick, 300);
    return () => {
      if (timer !== null) clearTimeout(timer);
    };
  }, [scrolling, mode, paused]);

  // --- Section-pause observer (continuous mode only) -----------------------
  React.useEffect(() => {
    if (!scrolling || mode !== "continuous" || sectionPauseSec <= 0) return;
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
  }, [scrolling, mode, sectionPauseSec]);

  // --- Tap-anywhere pause/resume -------------------------------------------
  React.useEffect(() => {
    if (!enabled || !active) return;
    const onClick = (e: MouseEvent) => {
      const target = e.target as HTMLElement | null;
      if (!target) return;
      if (target.closest(INTERACTIVE_SELECTOR)) return;
      togglePause();
    };
    document.addEventListener("click", onClick);
    return () => document.removeEventListener("click", onClick);
  }, [enabled, active, togglePause]);

  // --- Manual wheel / touch scroll pauses (continuous mode only) ----------
  // In block mode, manual scroll would conflict with smooth-scrollIntoView,
  // so we don't auto-pause there — the user can use the FAB to stop.
  React.useEffect(() => {
    if (!scrolling || mode !== "continuous") return;
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
  }, [scrolling, mode]);

  if (!enabled) return null;

  return (
    <div className="fixed right-5 bottom-20 z-40 sm:bottom-24 sm:right-6">
      {/* Paused hint pill */}
      <AnimatePresence>
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
      </AnimatePresence>
      <motion.button
        type="button"
        data-no-autoscroll-pause
        onClick={toggleActive}
        aria-label={active ? "Jeda gulir otomatis" : "Mulai gulir otomatis"}
        title={
          active
            ? mode === "continuous"
              ? "Gulir otomatis berjalan — ketuk layar untuk jeda"
              : "Gulir otomatis berjalan (mode blok) — ketuk layar untuk jeda"
            : mode === "continuous"
              ? "Mulai gulir otomatis (ketuk layar untuk jeda/lanjut)"
              : "Mulai gulir otomatis per blok (ketuk layar untuk jeda/lanjut)"
        }
        initial={false}
        whileHover={{ scale: 1.08 }}
        whileTap={{ scale: 0.92 }}
        // h-9 w-9 (36px) — 70% of the back-to-top FAB (h-12 w-12 = 48px).
        // Pinned to the right edge, above the back-to-top button.
        className={`flex h-9 w-9 items-center justify-center rounded-full shadow-lg transition-colors ${
          active
            ? "bg-gradient-to-br from-emerald-600 to-teal-700 text-white shadow-emerald-900/30 dark:from-emerald-500 dark:to-teal-600"
            : "border border-border bg-card text-foreground"
        }`}
      >
        {scrolling ? (
          <Pause className="h-4 w-4" strokeWidth={2.5} />
        ) : (
          <Play className="h-4 w-4 translate-x-0.5" strokeWidth={2.5} />
        )}
      </motion.button>
      {/* Pulsing ring while running so the user knows it's active. */}
      <AnimatePresence>
        {scrolling && (
          <motion.span
            aria-hidden
            initial={{ opacity: 0.5, scale: 1 }}
            animate={{ opacity: 0, scale: 1.8 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 1.4, repeat: Infinity, ease: "easeOut" }}
            className="pointer-events-none absolute inset-0 rounded-full bg-emerald-400/40"
          />
        )}
      </AnimatePresence>
    </div>
  );
}
