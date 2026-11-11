"use client";

import * as React from "react";
import { Header, MobileNav } from "@/components/tahlil/header";
import { Hero } from "@/components/tahlil/hero";
import { SectionCard } from "@/components/tahlil/section-card";
import { BackToTop } from "@/components/tahlil/back-to-top";
import { ScrollProgress } from "@/components/scroll-progress";
import { ArabicStylePanel } from "@/components/arabic-style-panel";
import { DisplaySettingsPanel } from "@/components/display-settings-panel";
import { tahlilSections } from "@/data/tahlil-content";
import { BUILD_INFO } from "@/data/build-info";

export default function Home() {
  const [navOpen, setNavOpen] = React.useState(false);
  const [arabicStyleOpen, setArabicStyleOpen] = React.useState(false);
  const [displayOpen, setDisplayOpen] = React.useState(false);

  // Lock scroll when mobile nav is open
  React.useEffect(() => {
    if (navOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }
    return () => {
      document.body.style.overflow = "";
    };
  }, [navOpen]);

  // Close on escape
  React.useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") setNavOpen(false);
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, []);

  return (
    <div className="relative min-h-screen flex flex-col">
      <ScrollProgress />
      <Header
        onOpenNav={() => setNavOpen(true)}
        onOpenArabicStyle={() => setArabicStyleOpen(true)}
        onOpenDisplay={() => setDisplayOpen(true)}
      />

      <main className="flex-1">
        <Hero />

        {/* Section list */}
        <div className="mx-auto max-w-3xl px-4 pb-20 sm:px-6 lg:px-8">
          <div className="space-y-8">
            {tahlilSections.map((section) => (
              <SectionCard key={section.id} section={section} />
            ))}
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-border bg-card/50 backdrop-blur">
        <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6 lg:px-8">
          <div className="flex flex-col items-center justify-between gap-4 sm:flex-row">
            <div className="text-center sm:text-left">
              <p className="text-sm font-semibold">
                Tahlil App — Tahlil Lengkap Malam Jumat
                <span
                  className="ms-2 inline-flex items-center rounded-full bg-muted px-2 py-0.5 align-middle text-[10px] font-medium text-muted-foreground"
                  title="Versi aplikasi — jika bukan versi terbaru, halaman ini cache lama atau deploy lama"
                >
                  v{BUILD_INFO.version}
                </span>
              </p>
              <p className="mt-1 text-xs text-muted-foreground">
                Disusun dari Al-Qur&apos;an, hadits, dan amaliah ulama.
              </p>
            </div>
            <div className="text-center sm:text-right">
              <p className="text-xs text-muted-foreground">
                Sumber konten: {""}
                <span className="font-medium text-foreground">Salik Channel</span>
              </p>
              <p className="mt-1 text-[11px] text-muted-foreground/70">
                Dibuat untuk kepentingan ibadah &amp; pembelajaran.
              </p>
            </div>
          </div>
        </div>
      </footer>

      {/* Backdrop for mobile nav */}
      {navOpen && (
        <div
          className="fixed inset-0 z-[65] bg-black/40 backdrop-blur-sm"
          onClick={() => setNavOpen(false)}
          aria-hidden="true"
        />
      )}
      <MobileNav open={navOpen} onClose={() => setNavOpen(false)} />

      <ArabicStylePanel open={arabicStyleOpen} onClose={() => setArabicStyleOpen(false)} />

      <DisplaySettingsPanel open={displayOpen} onClose={() => setDisplayOpen(false)} />

      <BackToTop />
    </div>
  );
}
