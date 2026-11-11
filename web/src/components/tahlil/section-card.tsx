"use client";

import * as React from "react";
import { motion, useInView } from "framer-motion";
import { type Section } from "@/data/tahlil-content";
import { ArabicText } from "@/components/arabic-text";
import { useDisplaySettings } from "@/components/display-settings-provider";

const typeConfig: Record<Section["type"], { label: string; color: string; ring: string }> = {
  pembukaan: {
    label: "Pembukaan",
    color: "bg-emerald-500/15 text-emerald-700 dark:text-emerald-300",
    ring: "ring-emerald-500/30",
  },
  sholawat: {
    label: "Shalawat",
    color: "bg-teal-500/15 text-teal-700 dark:text-teal-300",
    ring: "ring-teal-500/30",
  },
  surah: {
    label: "Surat",
    color: "bg-amber-500/15 text-amber-700 dark:text-amber-300",
    ring: "ring-amber-500/30",
  },
  tahlil: {
    label: "Tahlil",
    color: "bg-emerald-500/15 text-emerald-700 dark:text-emerald-300",
    ring: "ring-emerald-500/30",
  },
  ayat: {
    label: "Ayat",
    color: "bg-yellow-500/15 text-yellow-700 dark:text-yellow-300",
    ring: "ring-yellow-500/30",
  },
  doa: {
    label: "Doa",
    color: "bg-rose-500/15 text-rose-700 dark:text-rose-300",
    ring: "ring-rose-500/30",
  },
  dzikir: {
    label: "Dzikir",
    color: "bg-violet-500/15 text-violet-700 dark:text-violet-300",
    ring: "ring-violet-500/30",
  },
};

function ArabicOrnament({ className = "" }: { className?: string }) {
  return (
    <svg viewBox="0 0 60 12" className={className} fill="none" aria-hidden="true">
      <path
        d="M2 6 L18 6 M22 6 a4 4 0 1 0 8 0 a4 4 0 1 0 -8 0 M34 6 L40 6 M30 1 L40 6 L30 11 M42 6 a4 4 0 1 0 8 0 a4 4 0 1 0 -8 0 M52 6 L58 6"
        stroke="currentColor"
        strokeWidth="1"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

export function SectionCard({ section }: { section: Section }) {
  const ref = React.useRef<HTMLDivElement>(null);
  const inView = useInView(ref, { once: true, margin: "-80px 0px -80px 0px" });
  const cfg = typeConfig[section.type];
  const { toggles } = useDisplaySettings();
  const allHidden = !toggles.arabic && !toggles.transliteration && !toggles.translation;

  return (
    <motion.section
      id={section.id}
      ref={ref}
      initial={{ opacity: 0, y: 32 }}
      animate={inView ? { opacity: 1, y: 0 } : { opacity: 0, y: 32 }}
      transition={{ duration: 0.6, ease: [0.22, 1, 0.36, 1] }}
      className="scroll-mt-24"
    >
      <article className="group relative overflow-hidden rounded-3xl border border-border bg-card/80 shadow-sm transition-all duration-300 hover:shadow-xl hover:border-primary/40">
        {/* Top ornament strip */}
        <div className="h-1.5 w-full bg-gradient-to-r from-emerald-500 via-teal-400 to-amber-400 opacity-80" />

        <div className="p-5 sm:p-7 lg:p-9">
          {/* Header */}
          <div className="mb-6 flex flex-wrap items-start gap-4">
            <motion.span
              initial={{ scale: 0.6, opacity: 0 }}
              animate={inView ? { scale: 1, opacity: 1 } : {}}
              transition={{ delay: 0.1, type: "spring", stiffness: 200 }}
              className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-emerald-600 to-teal-700 font-bold text-white shadow-md dark:from-emerald-500 dark:to-teal-600"
            >
              {section.number}
            </motion.span>
            <div className="min-w-0 flex-1">
              <div className="flex flex-wrap items-center gap-2">
                <h2 className="text-xl font-bold tracking-tight text-foreground sm:text-2xl">
                  {section.title}
                </h2>
                <span
                  className={`rounded-full px-2.5 py-0.5 text-[11px] font-medium ring-1 ${cfg.color} ${cfg.ring}`}
                >
                  {cfg.label}
                </span>
              </div>
              {section.subtitle && (
                <p className="mt-1 text-sm text-muted-foreground">{section.subtitle}</p>
              )}
            </div>
          </div>

          {/* Ornamental divider */}
          <div className="ornament-divider mb-6 text-amber-500/70 dark:text-amber-400/60">
            <ArabicOrnament className="h-3 w-24" />
          </div>

          {/* Blocks */}
          <div className="space-y-7">
            {section.blocks.map((block, i) => (
              <motion.div
                key={i}
                initial={{ opacity: 0, y: 20 }}
                animate={inView ? { opacity: 1, y: 0 } : {}}
                transition={{ delay: 0.15 + i * 0.08, duration: 0.5 }}
                className="space-y-3"
              >
                {/* Arabic */}
                {block.arabic && toggles.arabic && (
                  <div className="relative">
                    <div className="rounded-2xl bg-gradient-to-br from-emerald-50/80 to-amber-50/60 p-5 dark:from-emerald-950/40 dark:to-amber-950/20 sm:p-6">
                      <ArabicText
                        text={block.arabic}
                        className="text-foreground"
                        suffix={
                          block.repeat && block.repeat > 1 ? (
                            <span className="ms-2 align-super text-base font-sans text-primary">
                              {" "}
                              ×{block.repeat}
                            </span>
                          ) : undefined
                        }
                      />
                    </div>
                  </div>
                )}

                {/* Transliteration */}
                {toggles.transliteration && (
                  <div className="px-1">
                    <p className="text-[15px] italic leading-relaxed text-primary/90 dark:text-primary">
                      {block.transliteration}
                    </p>
                  </div>
                )}

                {/* Translation */}
                {toggles.translation && (
                  <div className="px-1">
                    <p className="text-[15px] leading-relaxed text-foreground/80 dark:text-foreground/75">
                      {block.translation}
                    </p>
                  </div>
                )}

                {/* If all toggles off, show a subtle hint */}
                {allHidden && (
                  <div className="rounded-xl border border-dashed border-border bg-muted/30 px-4 py-3 text-xs text-muted-foreground">
                    Semua tampilan disembunyikan. Aktifkan minimal satu di tombol Tampilan.
                  </div>
                )}

                {/* Note */}
                {block.note && (
                  <div className="flex items-start gap-2 rounded-lg bg-muted/60 px-3 py-2 text-xs text-muted-foreground">
                    <span className="mt-0.5 text-primary">◆</span>
                    <span>{block.note}</span>
                  </div>
                )}
              </motion.div>
            ))}
          </div>
        </div>
      </article>
    </motion.section>
  );
}
