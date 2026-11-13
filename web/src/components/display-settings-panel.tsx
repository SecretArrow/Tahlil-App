"use client";

import * as React from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  Eye,
  EyeOff,
  Type,
  Languages,
  BookOpen,
  RotateCcw,
  ListPlus,
  Palette,
  WrapText,
  Highlighter,
  ArrowDownToLine,
  FastForward,
} from "lucide-react";
import { useDisplaySettings } from "@/components/display-settings-provider";
import {
  AUTO_SCROLL_PAUSE_CHOICES,
  AUTO_SCROLL_SPEED_MAX,
  AUTO_SCROLL_SPEED_MIN,
  AUTO_SCROLL_SPEED_STEP,
} from "@/data/display-settings";
import { ArabicText } from "@/components/arabic-text";
import { TajwidLegend } from "@/components/tajwid-legend";
import { PREVIEW_TAJWID_TEXT } from "@/lib/tajwid";

type Props = {
  open: boolean;
  onClose: () => void;
};

const PREVIEW_ARABIC = "بِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيْمِ";
const PREVIEW_ARABIC_LIST =
  "وَعَلَى آلِهِ وَأَصْحَابِهِ وَأَزْوَاجِهِ وَذُرِّيَّتِهِ وَأَهْلِ بَيْتِهِ";
const PREVIEW_LATIN = "Bismillaahirrahmaanir-rahiim.";
const PREVIEW_ID = "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang.";
// Contains Arabic commas (،) + a final period so the live preview shows the
// "Koma = Baris Baru" toggle in action: ON → one clause per line, OFF → a
// single flowing paragraph.
const PREVIEW_ARABIC_COMMAS =
  "اَلْحَمْدُ لِلَّهِ رَبِّ الْعٰلَمِيْنَ، وَالصَّلَاةُ وَالسَّلَامُ عَلَى رَسُوْلِ اللَّهِ، وَعَلَىٰ آلِهِ وَأَصْحَابِهِ أَجْمَعِيْنَ.";

function ToggleRow({
  label,
  description,
  icon: Icon,
  active,
  onToggle,
  activeLabel = "Tampil",
  inactiveLabel = "Sembunyi",
}: {
  label: string;
  description: string;
  icon: React.ElementType;
  active: boolean;
  onToggle: () => void;
  /** Badge text when active — override for non-visibility toggles. */
  activeLabel?: string;
  /** Badge text when inactive — override for non-visibility toggles. */
  inactiveLabel?: string;
}) {
  return (
    <button
      type="button"
      onClick={onToggle}
      aria-pressed={active}
      className={`flex w-full items-center gap-3 rounded-2xl border p-3 text-left transition-all ${
        active ? "border-primary/40 bg-primary/5" : "border-border bg-card/60 hover:bg-accent"
      }`}
    >
      <span
        className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl transition-colors ${
          active ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground"
        }`}
      >
        <Icon className="h-5 w-5" />
      </span>
      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-2">
          <span className="text-sm font-semibold text-foreground">{label}</span>
          <span
            className={`rounded-full px-1.5 py-0.5 text-[10px] font-medium uppercase tracking-wide ${
              active ? "bg-primary/15 text-primary" : "bg-muted text-muted-foreground"
            }`}
          >
            {active ? activeLabel : inactiveLabel}
          </span>
        </div>
        <p className="mt-0.5 text-xs text-muted-foreground">{description}</p>
      </div>
      <span
        className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors ${
          active ? "bg-primary" : "bg-muted-foreground/30"
        }`}
      >
        <motion.span
          layout
          transition={{ type: "spring", stiffness: 400, damping: 30 }}
          className={`inline-block h-5 w-5 transform rounded-full bg-white shadow ${
            active ? "translate-x-5" : "translate-x-0.5"
          }`}
        />
      </span>
    </button>
  );
}

export function DisplaySettingsPanel({ open, onClose }: Props) {
  const { toggles, setToggle, setNumber, setAutoScrollMode, reset } = useDisplaySettings();

  return (
    <AnimatePresence>
      {open && (
        <>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.18 }}
            className="fixed inset-0 z-[80] bg-black/30 backdrop-blur-sm"
            onClick={onClose}
            aria-hidden="true"
          />
          <motion.div
            initial={{ opacity: 0, y: -8, scale: 0.98 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -8, scale: 0.98 }}
            transition={{ duration: 0.2, ease: "easeOut" }}
            role="dialog"
            aria-label="Pengaturan tampilan bacaan"
            className="fixed left-1/2 top-20 z-[81] flex max-h-[calc(100dvh-7rem)] w-[92%] max-w-md -translate-x-1/2 flex-col overflow-hidden rounded-3xl border border-border bg-card shadow-2xl"
          >
            <div className="flex items-center justify-between border-b border-border p-4">
              <div className="flex items-center gap-2.5">
                <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-600 to-teal-700 text-white dark:from-emerald-500 dark:to-teal-600">
                  <Eye className="h-5 w-5" />
                </span>
                <div>
                  <h2 className="text-base font-bold">Tampilan Bacaan</h2>
                  <p className="text-xs text-muted-foreground">
                    Aktifkan / sembunyikan tiap bagian
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={onClose}
                aria-label="Tutup"
                className="inline-flex h-8 w-8 items-center justify-center rounded-full hover:bg-accent"
              >
                <EyeOff className="h-4 w-4" />
              </button>
            </div>

            <div className="flex min-h-0 flex-1 flex-col gap-3 overflow-y-auto overscroll-contain p-4">
              <ToggleRow
                label="Teks Arab"
                description="Tulisan Arab dengan harakat (font dapat diatur di Pengaturan Teks Arab)"
                icon={Type}
                active={toggles.arabic}
                onToggle={() => setToggle("arabic", !toggles.arabic)}
              />
              <ToggleRow
                label="Transliterasi Latin"
                description="Teks Arab dalam huruf Latin (mis: Bismillaahirrahmaanir-rahiim)"
                icon={Languages}
                active={toggles.transliteration}
                onToggle={() => setToggle("transliteration", !toggles.transliteration)}
              />
              <ToggleRow
                label="Terjemahan Indonesia"
                description="Arti/terjemahan bahasa Indonesia"
                icon={BookOpen}
                active={toggles.translation}
                onToggle={() => setToggle("translation", !toggles.translation)}
              />
              <ToggleRow
                label="Koma pada Teks Arab"
                description="Tambahkan koma (،) pada teks Arab mengikuti tanda koma di terjemahan — misalnya pada shalawat, daftar ahli kubur, dan doa. Ayat Al-Qur'an tidak diubah."
                icon={ListPlus}
                active={toggles.punctuateArabic}
                onToggle={() => setToggle("punctuateArabic", !toggles.punctuateArabic)}
              />
              <ToggleRow
                label="Koma = Baris Baru"
                description="Mulai baris baru pada setiap koma (،) dan titik (.) di teks Arab. Matikan agar teks mengalir sebagai satu paragraf."
                icon={WrapText}
                active={toggles.splitArabicLines}
                activeLabel="Baris Baru"
                inactiveLabel="Paragraf"
                onToggle={() => setToggle("splitArabicLines", !toggles.splitArabicLines)}
              />
              <ToggleRow
                label="Warnai Kata Arab"
                description="Setiap kata Arab diberi warna bergantian agar mudah dibedakan. Tema terang: #000000 & #011b45. Tema gelap: #ffffff & #d9dadb."
                icon={Palette}
                active={toggles.colorizeArabic}
                onToggle={() => setToggle("colorizeArabic", !toggles.colorizeArabic)}
              />
              <ToggleRow
                label="Tajwid"
                description="Warnai hukum bacaan pada teks Arab: Izhar, Ikhfa, Idgham, Iqlab, Ghunnah, dan Qalqalah. Teks tidak diubah, hanya diberi warna. Saat aktif, pewarnaan kata dinonaktifkan sementara."
                icon={Highlighter}
                active={toggles.tajwid}
                activeLabel="Aktif"
                inactiveLabel="Mati"
                onToggle={() => setToggle("tajwid", !toggles.tajwid)}
              />
              {toggles.tajwid && (
                <div className="rounded-2xl border border-border bg-muted/30 p-3">
                  <p className="mb-2 text-[10px] font-semibold uppercase tracking-wide text-muted-foreground">
                    Kode warna tajwid
                  </p>
                  <TajwidLegend />
                </div>
              )}

              {/* Auto scroll (Gulir Otomatis) */}
              <div className="rounded-2xl border border-border bg-muted/30 p-3">
                <div className="mb-2 flex items-center gap-2">
                  <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-primary/10 text-primary">
                    <FastForward className="h-4 w-4" />
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="text-sm font-semibold text-foreground">Gulir Otomatis</p>
                    <p className="text-xs text-muted-foreground">
                      Aktifkan untuk menampilkan tombol play/pause di kanan layar. Ketuk layar 1×
                      atau tahan = jeda; ketuk lagi = lanjut.
                    </p>
                  </div>
                  {/* Master enable/disable switch */}
                  <button
                    type="button"
                    onClick={() => setToggle("autoScrollEnabled", !toggles.autoScrollEnabled)}
                    aria-pressed={toggles.autoScrollEnabled}
                    aria-label="Aktifkan Gulir Otomatis"
                    className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors ${
                      toggles.autoScrollEnabled ? "bg-primary" : "bg-muted-foreground/30"
                    }`}
                  >
                    <motion.span
                      layout
                      transition={{ type: "spring", stiffness: 400, damping: 30 }}
                      className={`inline-block h-5 w-5 transform rounded-full bg-white shadow ${
                        toggles.autoScrollEnabled ? "translate-x-5" : "translate-x-0.5"
                      }`}
                    />
                  </button>
                </div>

                {toggles.autoScrollEnabled && (
                  <>
                    {/* Mode picker */}
                    <div className="mt-1 mb-3">
                      <p className="mb-1 text-xs font-medium text-foreground">Mode Gulir</p>
                      <div className="grid grid-cols-2 gap-2">
                        <button
                          type="button"
                          onClick={() => setAutoScrollMode("continuous")}
                          aria-pressed={toggles.autoScrollMode === "continuous"}
                          className={`flex flex-col items-start gap-0.5 rounded-xl border p-2 text-left transition-all ${
                            toggles.autoScrollMode === "continuous"
                              ? "border-primary bg-primary/5"
                              : "border-border hover:bg-accent"
                          }`}
                        >
                          <span className="flex items-center gap-1.5 text-xs font-semibold text-foreground">
                            <FastForward className="h-3.5 w-3.5" />
                            Terus-menerus
                          </span>
                          <span className="text-[11px] leading-snug text-muted-foreground">
                            Halaman menggulir halus tanpa henti.
                          </span>
                        </button>
                        <button
                          type="button"
                          onClick={() => setAutoScrollMode("block")}
                          aria-pressed={toggles.autoScrollMode === "block"}
                          className={`flex flex-col items-start gap-0.5 rounded-xl border p-2 text-left transition-all ${
                            toggles.autoScrollMode === "block"
                              ? "border-primary bg-primary/5"
                              : "border-border hover:bg-accent"
                          }`}
                        >
                          <span className="flex items-center gap-1.5 text-xs font-semibold text-foreground">
                            <ArrowDownToLine className="h-3.5 w-3.5" />
                            Per Blok
                          </span>
                          <span className="text-[11px] leading-snug text-muted-foreground">
                            Lompat dari satu bagian ke bagian berikutnya.
                          </span>
                        </button>
                      </div>
                    </div>

                    {/* Speed slider — 0.1 to 25 */}
                    <label className="mt-1 block">
                      <span className="flex items-center justify-between text-xs font-medium text-foreground">
                        Kecepatan Gulir
                        <span className="font-semibold text-primary">
                          {toggles.autoScrollSpeed.toFixed(1)}
                        </span>
                      </span>
                      <input
                        type="range"
                        min={AUTO_SCROLL_SPEED_MIN}
                        max={AUTO_SCROLL_SPEED_MAX}
                        step={AUTO_SCROLL_SPEED_STEP}
                        value={toggles.autoScrollSpeed}
                        onChange={(e) => setNumber("autoScrollSpeed", Number(e.target.value))}
                        className="mt-1 w-full accent-primary"
                      />
                      <div className="flex justify-between text-[10px] text-muted-foreground">
                        <span>0.1 (pelan)</span>
                        <span>25 (cepat)</span>
                      </div>
                    </label>

                    {/* Section pause — only meaningful in continuous mode */}
                    {toggles.autoScrollMode === "continuous" && (
                      <div className="mt-2">
                        <p className="text-xs font-medium text-foreground">Jeda per Bagian</p>
                        <p className="text-[11px] text-muted-foreground">
                          Berhenti sesaat setiap masuk bagian baru (hanya mode Terus-menerus)
                        </p>
                        <div className="mt-1.5 flex flex-wrap gap-1.5">
                          {AUTO_SCROLL_PAUSE_CHOICES.map((sec) => {
                            const isActive = toggles.autoScrollSectionPauseSec === sec;
                            return (
                              <button
                                key={sec}
                                type="button"
                                onClick={() => setNumber("autoScrollSectionPauseSec", sec)}
                                className={`rounded-full px-3 py-1 text-xs font-medium transition-colors ${
                                  isActive
                                    ? "bg-primary text-primary-foreground"
                                    : "border border-border bg-card text-muted-foreground hover:bg-accent"
                                }`}
                              >
                                {sec === 0 ? "Mati" : `${sec} dtk`}
                              </button>
                            );
                          })}
                        </div>
                      </div>
                    )}
                  </>
                )}
              </div>

              {/* Live preview */}
              <div className="rounded-2xl border border-border bg-gradient-to-br from-emerald-50/80 to-amber-50/60 p-4 dark:from-emerald-950/40 dark:to-amber-950/20">
                <p className="mb-2 text-[10px] font-semibold uppercase tracking-wide text-muted-foreground">
                  Pratinjau Langsung
                </p>
                <div className="space-y-2">
                  {toggles.arabic && (
                    <>
                      <ArabicText text={PREVIEW_ARABIC} className="text-foreground/90" />
                      {toggles.punctuateArabic && (
                        <ArabicText text={PREVIEW_ARABIC_LIST} className="text-foreground/90" />
                      )}
                      <ArabicText text={PREVIEW_ARABIC_COMMAS} className="text-foreground/90" />
                      {toggles.tajwid && (
                        <ArabicText text={PREVIEW_TAJWID_TEXT} className="text-foreground/90" />
                      )}
                    </>
                  )}
                  {toggles.transliteration && (
                    <p className="text-[13px] italic text-primary/90 dark:text-primary">
                      {PREVIEW_LATIN}
                    </p>
                  )}
                  {toggles.translation && (
                    <p className="text-[13px] text-foreground/75">{PREVIEW_ID}</p>
                  )}
                  {!toggles.arabic && !toggles.transliteration && !toggles.translation && (
                    <p className="text-xs italic text-muted-foreground">
                      Semua tampilan disembunyikan. Aktifkan minimal satu.
                    </p>
                  )}
                </div>
              </div>

              <div className="flex items-center justify-between border-t border-border pt-3">
                <p className="text-xs text-muted-foreground">Tersimpan otomatis di browser ini.</p>
                <button
                  type="button"
                  onClick={reset}
                  className="inline-flex items-center gap-1.5 rounded-full border border-border bg-card px-3 py-1.5 text-xs font-medium text-muted-foreground transition-colors hover:bg-accent hover:text-foreground"
                >
                  <RotateCcw className="h-3.5 w-3.5" />
                  Reset
                </button>
              </div>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}
