"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthUser, getAuthRole, getJwt } from "@/shared/auth";
import {
  VerifySearchResponse,
  fetchSearchHistory,
  verifySerialNumber,
} from "@/features/verification/lib/verify";

function formatDate(value: string): string {
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleString();
}

function inferEvidenceType(
  fileType: string | null | undefined,
  fileUrl?: string | null,
): "IMAGE" | "PDF" {
  if (fileType === "PDF") return "PDF";
  if (fileType === "IMAGE") return "IMAGE";
  if (fileUrl && /\.pdf(\?|$)/i.test(fileUrl)) return "PDF";
  return "IMAGE";
}

async function fetchAndFilterRecentSearches() {
  try {
    const history = await fetchSearchHistory(0, 20);
    const clearedAtStr = localStorage.getItem("verifyRecentSearchesClearedAt");
    let clearedAt = 0;
    if (clearedAtStr) {
      clearedAt = new Date(clearedAtStr).getTime();
    }
    return history.filter(
      (item) => new Date(item.timestamp).getTime() > clearedAt
    ).slice(0, 5);
  } catch {
    return [];
  }
}

export default function VerifyItemPage() {
  const router = useRouter();
  const [serial, setSerial] = useState("");
  const [result, setResult] = useState<VerifySearchResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [recentSearches, setRecentSearches] = useState<
    Array<{
      searchedSerial: string;
      result: "CLEAN" | "STOLEN";
      timestamp: string;
    }>
  >([]);

  const [viewerFile, setViewerFile] = useState<{
    url: string;
    type: "IMAGE" | "PDF";
  } | null>(null);

  useEffect(() => {
    function handleEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setViewerFile(null);
      }
    }
    if (viewerFile) {
      document.body.style.overflow = "hidden";
      document.addEventListener("keydown", handleEscape);
    }
    return () => {
      document.body.style.overflow = "";
      document.removeEventListener("keydown", handleEscape);
    };
  }, [viewerFile]);

  const userRole = useMemo(() => {
    if (typeof window === "undefined") {
      return "";
    }

    return getAuthRole() || "";
  }, []);

  useEffect(() => {
    const token = getJwt();
    const authenticatedEmail = getAuthUser();

    if (!token && !authenticatedEmail) {
      router.replace("/login");
      return;
    }

    if (userRole !== "BUSINESS") {
      router.replace("/dashboard");
      return;
    }

    fetchAndFilterRecentSearches().then(setRecentSearches);
  }, [router, userRole]);

  async function handleVerify(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage("");
    setResult(null);

    const trimmedSerial = serial.trim();
    if (!trimmedSerial) {
      setErrorMessage("Serial number is required.");
      return;
    }

    setIsLoading(true);
    try {
      const response = await verifySerialNumber(trimmedSerial);
      setResult(response);

      const filteredHistory = await fetchAndFilterRecentSearches();
      setRecentSearches(filteredHistory);
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "Verification failed.",
      );
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="min-h-screen text-slate-200">
      <main className="mx-auto w-full max-w-5xl px-4 pb-16 pt-36 sm:px-6 sm:pt-40 md:pt-32 lg:px-8">
        <section className="glass-panel rounded-2xl bg-slate-900/35 p-6 shadow-[0_18px_36px_rgba(0,0,0,0.28)] sm:p-8">
          <h1 className="mt-2 text-3xl font-bold text-white sm:text-4xl">
            Verify if an item is stolen
          </h1>
          <p className="mt-4 max-w-3xl text-sm text-slate-300 sm:text-base">
            Enter a serial number to check PawnScan approved stolen records and
            Bike Index's public stolen-bike registry. Every lookup is logged
            automatically to your business history.
          </p>

          <form
            onSubmit={handleVerify}
            className="mt-8 grid gap-4 sm:grid-cols-[1fr_auto]"
          >
            <div className="relative">
              <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-4">
                <svg
                  className="h-5 w-5 text-slate-400"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
              </div>
              <input
                type="text"
                value={serial}
                onChange={(event) => setSerial(event.target.value)}
                placeholder="Enter item serial number (e.g. SN-123456789)"
                className="w-full rounded-xl border border-slate-700/60 bg-slate-900/70 py-4 pl-12 pr-4 text-sm text-slate-100 placeholder-slate-400 outline-none transition focus:border-brand/70 focus:ring-2 focus:ring-brand/25"
              />
            </div>
            <button
              type="submit"
              disabled={isLoading}
              className="flex items-center justify-center gap-2 rounded-xl bg-brand px-7 py-4 text-sm font-bold text-bg-main transition-all duration-200 ease-out hover:brightness-110 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isLoading ? "Checking..." : "Verify Item"}
            </button>
          </form>

          {errorMessage && (
            <div className="mt-4 rounded-xl border border-red-400/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
              {errorMessage}
            </div>
          )}

          {result && (
            <div
              className={`mt-10 overflow-hidden rounded-3xl border bg-slate-900/55 p-8 shadow-2xl transition-all duration-500 animate-in fade-in slide-in-from-bottom-4 ${
                result.status === "STOLEN"
                  ? "border-status-stolen/30 shadow-[0_0_40px_rgba(239,68,68,0.15)]"
                  : "border-status-clean/30 shadow-[0_0_40px_rgba(16,185,129,0.15)]"
              }`}
            >
              <div className="flex flex-col items-center justify-between gap-6 sm:flex-row sm:text-left">
                <div>
                  <p className="text-xs font-bold uppercase tracking-[0.2em] text-slate-400 mb-1.5">
                    Verification Result
                  </p>
                  <p className="text-xl text-slate-300">
                    Serial searched:{" "}
                    <span className="font-mono font-bold text-white tracking-tight">
                      {result.serial}
                    </span>
                  </p>
                </div>
                <div
                  className={`inline-flex items-center gap-2 rounded-full border px-5 py-2 text-sm font-black uppercase tracking-widest shadow-lg ${
                    result.status === "STOLEN"
                      ? "border-status-stolen/40 bg-status-stolen/10 text-status-stolen shadow-status-stolen/20"
                      : "border-status-clean/40 bg-status-clean/10 text-status-clean shadow-status-clean/20"
                  }`}
                >
                  {result.status === "STOLEN" ? (
                    <span className="relative flex h-2.5 w-2.5">
                      <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-status-stolen opacity-75"></span>
                      <span className="relative inline-flex h-2.5 w-2.5 rounded-full bg-status-stolen"></span>
                    </span>
                  ) : (
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7"/></svg>
                  )}
                  {result.status}
                </div>
              </div>

              {result.status === "STOLEN" && result.report && (
                <div className="mt-8 rounded-2xl border border-status-stolen/20 bg-status-stolen/5 p-6 sm:p-8 backdrop-blur-sm relative overflow-hidden">
                  <div className="absolute top-0 right-0 p-32 bg-status-stolen/10 rounded-full blur-[100px] pointer-events-none" />
                  
                  <h3 className="mb-6 flex items-center gap-2 text-sm font-black uppercase tracking-widest text-status-stolen">
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
                    Stolen Report Details
                  </h3>
                  
                  <div className="grid gap-6 sm:grid-cols-2 relative z-10">
                    <div className="space-y-1">
                      <p className="text-xs uppercase tracking-widest text-slate-500 font-semibold">Date Reported</p>
                      <p className="text-lg font-bold text-white">
                        {formatDate(result.report.dateReported)}
                      </p>
                    </div>
                    <div className="space-y-1">
                      <p className="text-xs uppercase tracking-widest text-slate-500 font-semibold">Owner Name</p>
                      <p className="text-lg font-bold text-white">
                        {result.report.ownerName || result.report.victimName || "Information Not Disclosed"}
                      </p>
                    </div>
                    <div className="space-y-1">
                      <p className="text-xs uppercase tracking-widest text-slate-500 font-semibold">Owner Email</p>
                      <p className="text-base font-bold text-white truncate break-all">
                        {result.report.ownerEmail || "Not Disclosed"}
                      </p>
                    </div>
                    <div className="space-y-1">
                      <p className="text-xs uppercase tracking-widest text-slate-500 font-semibold">Phone Number</p>
                      <p className="text-base font-bold text-white truncate">
                        {result.report.ownerPhoneNumber || "Not Disclosed"}
                      </p>
                    </div>
                    <div className="space-y-1 sm:col-span-2">
                      <p className="text-xs uppercase tracking-widest text-slate-500 font-semibold">Item Model</p>
                      <p className="text-lg font-bold text-white">
                        {result.report.itemModel || "Unspecified Model"}
                      </p>
                    </div>
                    <div className="space-y-1 sm:col-span-2">
                      <p className="text-xs uppercase tracking-widest text-slate-500 font-semibold">Description</p>
                      <p className="text-base text-slate-300 leading-relaxed whitespace-pre-wrap">
                        {result.report.description || "No additional description provided."}
                      </p>
                    </div>

                    <div className="space-y-1 sm:col-span-2">
                      <p className="text-xs uppercase tracking-widest text-slate-500 font-semibold mb-2">Uploaded Evidence</p>
                      {!result.report.files || result.report.files.length === 0 ? (
                        <p className="text-sm text-slate-400 italic">No evidence file was uploaded for this report.</p>
                      ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-2">
                          {result.report.files.map((file, idx) => {
                            const type = inferEvidenceType(file.fileType, file.fileUrl);
                            const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";
                            const fullUrl = `${backendUrl}${file.fileUrl}`;
                            
                            return (
                              <button
                                key={file.id || idx}
                                type="button"
                                onClick={() => setViewerFile({ url: fullUrl, type })}
                                className="group relative flex h-32 w-full flex-col items-center justify-center overflow-hidden rounded-xl border border-slate-700/60 bg-slate-900/50 transition-all hover:border-brand/40 hover:bg-slate-800"
                              >
                                {type === "PDF" ? (
                                  <div className="flex flex-col items-center gap-2 text-slate-400 group-hover:text-slate-200 transition-colors">
                                    <svg className="h-8 w-8 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                                    </svg>
                                    <span className="text-xs font-semibold">View PDF Document</span>
                                  </div>
                                ) : (
                                  <>
                                    <img src={fullUrl} alt="Evidence preview" className="absolute inset-0 h-full w-full object-cover opacity-60 transition-opacity group-hover:opacity-40" />
                                    <div className="absolute inset-0 flex items-center justify-center bg-black/20 opacity-0 transition-opacity group-hover:opacity-100">
                                      <span className="rounded-lg bg-black/60 px-3 py-1.5 text-xs font-semibold text-white backdrop-blur-sm shadow-lg border border-white/10">View Image</span>
                                    </div>
                                  </>
                                )}
                              </button>
                            );
                          })}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              )}

              {result.status === "STOLEN" && !result.report && result.publicApiStolen && (
                <div className="mt-8 rounded-2xl border border-status-stolen/20 bg-status-stolen/5 p-6 sm:p-8 backdrop-blur-sm">
                  <h3 className="mb-4 flex items-center gap-2 text-sm font-black uppercase tracking-widest text-status-stolen">
                    Public Registry Match
                  </h3>
                  <div className="space-y-3">
                    <p className="text-sm text-slate-300">
                      {result.publicApiSource || "Public API"} found a stolen
                      listing for this serial number.
                    </p>
                    {result.publicApiMatchTitle && (
                      <p className="text-lg font-bold text-white">
                        {result.publicApiMatchTitle}
                      </p>
                    )}
                    {result.publicApiMatchUrl && (
                      <a
                        href={result.publicApiMatchUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="inline-flex text-sm font-semibold text-brand transition hover:text-brand/80"
                      >
                        View public match
                      </a>
                    )}
                  </div>
                </div>
              )}
            </div>
          )}
        </section>

        <section className="mt-8 glass-panel rounded-2xl bg-slate-900/35 p-6 shadow-[0_8px_22px_rgba(0,0,0,0.2)] sm:p-8">
          <div className="flex items-center justify-between gap-3 border-b border-slate-800 pb-4">
            <h2 className="text-lg font-bold text-white">Recent Searches</h2>
            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={() => {
                  setRecentSearches([]);
                  localStorage.setItem("verifyRecentSearchesClearedAt", new Date().toISOString());
                }}
                className="rounded-lg border border-slate-700/50 px-3 py-1.5 text-xs font-semibold text-slate-400 transition hover:bg-slate-800/70 hover:text-slate-200"
              >
                Clear
              </button>
            </div>
          </div>

          {recentSearches.length === 0 ? (
            <p className="mt-4 text-sm text-slate-400">
              No recent searches to display.
            </p>
          ) : (
            <ul className="mt-4 space-y-2">
              {recentSearches.map((item, index) => (
                <li
                  key={`${item.timestamp}-${item.searchedSerial}-${index}`}
                  className="flex items-center gap-4 rounded-xl bg-slate-900/50 px-5 py-3.5 text-sm transition-colors hover:bg-slate-800/60 border border-slate-800/50"
                >
                  <div className="flex-1 min-w-0 pr-4">
                    <span className="font-mono font-bold text-slate-200 block truncate text-base">{item.searchedSerial}</span>
                  </div>
                  <div className="w-32 flex-shrink-0">
                    <span
                      className={`inline-flex w-full items-center justify-center rounded-full px-3 py-1.5 text-[11px] font-bold uppercase tracking-widest ${
                        item.result === "STOLEN"
                          ? "bg-status-stolen/15 text-status-stolen border border-status-stolen/20 shadow-[0_0_10px_rgba(239,68,68,0.1)]"
                          : "bg-status-clean/15 text-status-clean border border-status-clean/20"
                      }`}
                    >
                      {item.result === "STOLEN" && (
                        <span className="relative flex h-2 w-2 mr-1.5">
                          <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-status-stolen opacity-75"></span>
                          <span className="relative inline-flex h-2 w-2 rounded-full bg-status-stolen"></span>
                        </span>
                      )}
                      {item.result}
                    </span>
                  </div>
                  <div className="w-44 flex-shrink-0 text-right">
                    <span className="text-xs font-medium text-slate-400 whitespace-nowrap bg-slate-900 px-2 py-1 rounded-md border border-slate-800">
                      {formatDate(item.timestamp)}
                    </span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>

      {viewerFile && (
        <div
          className="fixed inset-0 z-[80] flex items-center justify-center bg-slate-950/85 p-4 backdrop-blur"
          role="dialog"
          aria-modal="true"
          onClick={() => setViewerFile(null)}
        >
          <div
            className="relative w-full max-w-5xl rounded-2xl border border-slate-700 bg-slate-900 shadow-2xl"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="flex items-center justify-between border-b border-slate-700 px-4 py-3">
              <h3 className="text-sm font-semibold uppercase tracking-[0.15em] text-slate-200">
                Evidence Viewer
              </h3>
              <button
                type="button"
                onClick={() => setViewerFile(null)}
                className="rounded-md border border-slate-600 px-2.5 py-1 text-xs font-semibold text-slate-300 hover:bg-slate-800"
              >
                Close
              </button>
            </div>
            <div className="max-h-[80vh] overflow-auto p-4">
              {viewerFile.type === "PDF" ? (
                <iframe
                  src={viewerFile.url}
                  title="Evidence PDF"
                  className="h-[75vh] w-full rounded-lg border border-slate-700"
                />
              ) : (
                <img
                  src={viewerFile.url}
                  alt="Evidence"
                  className="mx-auto h-auto max-h-[75vh] w-auto rounded-lg border border-slate-700"
                />
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}


