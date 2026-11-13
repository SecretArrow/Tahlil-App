"use client";

import * as React from "react";
import { motion } from "framer-motion";
import { heroInfo } from "@/data/tahlil-content";

type CtaButton = {
  href: string;
  label: string;
  variant: "primary" | "secondary";
};

const ctaButtons: CtaButton[] = [
  { href: "#pembukaan", label: "Mulai", variant: "primary" },
  { href: "#tahlil-ahli-kubur", label: "Tahlil", variant: "secondary" },
  { href: "#dzikir", label: "Dzikir", variant: "secondary" },
  { href: "#doa-pembuka", label: "Doa", variant: "secondary" },
];

export function Hero() {
  return (
    <section id="top" className="relative overflow-hidden pt-28 pb-14 sm:pt-32 lg:pt-40 lg:pb-20">
      {/* Decorative background orbs */}
      <div className="pointer-events-none absolute inset-0 -z-10 overflow-hidden">
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 1.2 }}
          className="absolute -top-32 -right-24 h-72 w-72 rounded-full bg-emerald-500/15 blur-3xl dark:bg-emerald-500/20"
        />
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 1.2, delay: 0.2 }}
          className="absolute top-20 -left-20 h-72 w-72 rounded-full bg-amber-400/15 blur-3xl dark:bg-amber-400/15"
        />
      </div>

      <div className="mx-auto max-w-4xl px-4 text-center sm:px-6 lg:px-8">
        {/* Title */}
        <motion.h1
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.05 }}
          className="text-4xl font-bold tracking-tight text-foreground sm:text-5xl lg:text-6xl"
        >
          {heroInfo.title}
        </motion.h1>

        {/* Subtitle (only when non-empty) */}
        {heroInfo.subtitle && (
          <motion.p
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.1 }}
            className="mt-3 text-lg font-medium text-primary sm:text-xl"
          >
            {heroInfo.subtitle}
          </motion.p>
        )}

        {/* Bismillah */}
        <motion.p
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.15 }}
          dir="rtl"
          lang="ar"
          className="mt-6 font-arabic text-2xl leading-relaxed text-foreground sm:text-3xl"
        >
          {heroInfo.arabicBismillah}
        </motion.p>

        {/* Description */}
        <motion.p
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2 }}
          className="mx-auto mt-4 max-w-2xl text-sm text-muted-foreground sm:text-base"
        >
          {heroInfo.description}
        </motion.p>

        {/* CTA buttons */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.3 }}
          className="mt-9 flex flex-wrap items-center justify-center gap-3"
        >
          {ctaButtons.map((btn) => (
            <a
              key={btn.href}
              href={btn.href}
              className={
                btn.variant === "primary"
                  ? "inline-flex items-center justify-center rounded-full bg-primary px-6 py-3 text-sm font-semibold text-primary-foreground shadow-md transition-all hover:shadow-lg hover:brightness-110 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                  : "inline-flex items-center justify-center rounded-full border border-border bg-card/60 px-6 py-3 text-sm font-semibold text-foreground backdrop-blur transition-all hover:bg-accent"
              }
            >
              {btn.label}
            </a>
          ))}
        </motion.div>
      </div>
    </section>
  );
}
