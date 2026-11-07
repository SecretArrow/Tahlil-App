"use client";

import * as React from "react";
import { motion, useScroll, useMotionValueEvent } from "framer-motion";
import { Menu, Moon, BookOpen, X, Type, Eye } from "lucide-react";
import { ThemeToggle } from "@/components/theme-toggle";
import { tahlilSections } from "@/data/tahlil-content";


type HeaderProps = {
  onOpenNav: () => void;
  onOpenArabicStyle: () => void;
  onOpenDisplay: () => void;
};

export function Header({ onOpenNav, onOpenArabicStyle, onOpenDisplay }: HeaderProps) {
  const { scrollY } = useScroll();
  const [scrolled, setScrolled] = React.useState(false);
  const [activeId, setActiveId] = React.useState<string>("");

  useMotionValueEvent(scrollY, "change", (v) => {
    setScrolled(v > 24);
  });

  // Track active section
  React.useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            setActiveId(entry.target.id);
          }
        });
      },
      { rootMargin: "-30% 0px -55% 0px", threshold: 0 }
    );
    tahlilSections.forEach((s) => {
      const el = document.getElementById(s.id);
      if (el) observer.observe(el);
    });
    return () => observer.disconnect();
  }, []);

  return (
    <motion.header
      initial={{ y: -80, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.5, ease: "easeOut" }}
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        scrolled
          ? "glass border-b border-border/60 shadow-sm"
          : "bg-transparent"
      }`}
    >
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6 lg:px-8">
        <a
          href="#top"
          className="flex items-center gap-2.5 text-foreground transition-opacity hover:opacity-80"
        >
          <motion.span
            initial={{ rotate: -180, opacity: 0 }}
            animate={{ rotate: 0, opacity: 1 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-600 to-teal-700 text-white shadow-md dark:from-emerald-500 dark:to-teal-600"
          >
            <BookOpen className="h-5 w-5" strokeWidth={2.2} />
          </motion.span>
          <div className="flex flex-col leading-none">
            <span className="font-semibold tracking-tight">Tahlil Singkat</span>
            <span className="text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
              Majelis Doa
            </span>
          </div>
        </a>

        {/* Desktop nav */}
        <nav className="hidden lg:flex items-center gap-1.5">
          {tahlilSections.slice(0, 6).map((s) => {
            // Disambiguate short labels so the two Tahlil sections don't
            // both render as "Tahlil". Section 3 keeps "Tahlil" (the main
            // recitation); section 4 ("Tahlil & Surat-Surat") becomes
            // "Surat-Surat". Section 6 shows its full short title.
            const label =
              s.id === "tahlil-1"
                ? "Surat-Surat"
                : s.id === "ayat-kursi"
                  ? "Ayat Kursi"
                  : s.title.split(" ")[0];
            const isActive = activeId === s.id;
            return (
              <a
                key={s.id}
                href={`#${s.id}`}
                className={`group flex items-center gap-1.5 rounded-full border px-2.5 py-1.5 text-[13px] font-medium transition-all duration-200 ${
                  isActive
                    ? "border-primary/30 bg-primary/10 text-primary shadow-sm"
                    : "border-border/60 bg-background/40 text-muted-foreground hover:border-primary/30 hover:bg-accent hover:text-foreground"
                }`}
              >
                <span
                  className={`flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-[10px] font-bold leading-none transition-colors ${
                    isActive
                      ? "bg-primary text-primary-foreground"
                      : "bg-muted text-muted-foreground group-hover:bg-primary/15 group-hover:text-primary"
                  }`}
                >
                  {s.number}
                </span>
                <span>{label}</span>
              </a>
            );
          })}
        </nav>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={onOpenDisplay}
            aria-label="Pengaturan tampilan bacaan"
            title="Tampilan bacaan"
            className="inline-flex h-10 w-10 items-center justify-center rounded-full border border-border bg-background/80 backdrop-blur-md transition-colors hover:bg-accent"
          >
            <Eye className="h-5 w-5" />
          </button>
          <button
            type="button"
            onClick={onOpenArabicStyle}
            aria-label="Pengaturan teks Arab"
            title="Pengaturan teks Arab"
            className="inline-flex h-10 w-10 items-center justify-center rounded-full border border-border bg-background/80 backdrop-blur-md transition-colors hover:bg-accent"
          >
            <Type className="h-5 w-5" />
          </button>
          <ThemeToggle />
          <button
            type="button"
            onClick={onOpenNav}
            aria-label="Buka daftar isi"
            className="inline-flex h-10 w-10 items-center justify-center rounded-full border border-border bg-background/80 backdrop-blur-md transition-colors hover:bg-accent"
          >
            <Menu className="h-5 w-5" />
          </button>
        </div>
      </div>
    </motion.header>
  );
}

export function MobileNav({
  open,
  onClose,
}: {
  open: boolean;
  onClose: () => void;
}) {
  return (
    <motion.div
      initial={false}
      animate={{ x: open ? 0 : "100%" }}
      transition={{ type: "spring", stiffness: 320, damping: 34 }}
      className="fixed inset-y-0 right-0 z-[70] w-[82%] max-w-sm glass border-l border-border shadow-2xl"
      aria-hidden={!open}
    >
      <div className="flex h-full flex-col">
        <div className="flex items-center justify-between border-b border-border p-4">
          <div>
            <p className="font-semibold">Daftar Isi</p>
            <p className="text-xs text-muted-foreground">
              {tahlilSections.length} bagian
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Tutup daftar isi"
            className="inline-flex h-9 w-9 items-center justify-center rounded-full hover:bg-accent"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <nav className="flex-1 overflow-y-auto p-3">
          {tahlilSections.map((s) => (
            <a
              key={s.id}
              href={`#${s.id}`}
              onClick={onClose}
              className="flex items-start gap-3 rounded-xl px-3 py-2.5 transition-colors hover:bg-accent"
            >
              <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-primary/10 text-xs font-semibold text-primary">
                {s.number}
              </span>
              <span className="flex flex-col">
                <span className="text-sm font-medium leading-tight">
                  {s.title}
                </span>
                {s.subtitle && (
                  <span className="text-xs text-muted-foreground leading-tight mt-0.5">
                    {s.subtitle}
                  </span>
                )}
              </span>
            </a>
          ))}
        </nav>
        <div className="border-t border-border p-4 text-center text-xs text-muted-foreground">
          Sumber: Salik Channel
        </div>
      </div>
    </motion.div>
  );
}
