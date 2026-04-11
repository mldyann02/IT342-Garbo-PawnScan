"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthUser, getAuthRole, getJwt } from "@/lib/auth";
import BusinessDashboardHeader from "@/components/business-dashboard-header";
import {
  SearchLog,
  StolenMatch,
  fetchSearchHistory,
  fetchStolenMatches,
} from "@/lib/verify";

const PAGE_SIZE = 20;

function formatDate(value: string): string {
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleString();
}

function inferEvidenceType(
  fileType: StolenMatch["evidenceFileType"],
  fileUrl?: string | null,
): "IMAGE" | "PDF" {
  if (fileType === "PDF") {
    return "PDF";
  }

  if (fileType === "IMAGE") {
    return "IMAGE";
  }

  if (fileUrl && /\.pdf(\?|$)/i.test(fileUrl)) {
    return "PDF";
  }

  return "IMAGE";
}

export default function SearchHistoryPage() {
  const router = useRouter();
  const [history, setHistory] = useState<SearchLog[]>([]);
  const [matches, setMatches] = useState<StolenMatch[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [page, setPage] = useState(0);
  const [activeTab, setActiveTab] = useState<"searches" | "matches">(
    "searches",
  );
  const [selectedMatchId, setSelectedMatchId] = useState<number | null>(null);
  const [viewerFile, setViewerFile] = useState<{
    url: string;
    type: "IMAGE" | "PDF";
  } | null>(null);

  const selectedMatch = useMemo(
    () =>
      matches.find((item) => item.matchedReportId === selectedMatchId) ??
      matches[0] ??
      null,
    [matches, selectedMatchId],
  );

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

    // Redirect non-business users
    if (userRole !== "BUSINESS") {
      router.replace("/dashboard");
      return;
    }

    async function loadData() {
      setIsLoading(true);
      setErrorMessage("");
      try {
        const [historyResponse, matchesResponse] = await Promise.all([
          fetchSearchHistory(page, PAGE_SIZE),
          fetchStolenMatches(page, PAGE_SIZE),
        ]);

        setHistory(historyResponse);
        setMatches(matchesResponse);
        setSelectedMatchId(matchesResponse[0]?.matchedReportId ?? null);
      } catch (error) {
        setErrorMessage(
          error instanceof Error
            ? error.message
            : "Unable to load search history.",
        );
      } finally {
        setIsLoading(false);
      }
    }

    loadData();
  }, [page, router, userRole]);

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

  return (
    <div className="min-h-screen bg-gradient-to-b from-bg-main to-[#071022] text-slate-200">
      <BusinessDashboardHeader />

      <main className="mx-auto w-full max-w-6xl px-4 pb-16 pt-36 sm:px-6 sm:pt-40 md:pt-32 lg:px-8">
        <section className="glass-panel rounded-2xl bg-slate-900/35 p-6 shadow-[0_18px_36px_rgba(0,0,0,0.28)] sm:p-8">
          <h1 className="mt-2 text-3xl font-bold text-white sm:text-4xl">
            Business verification audit trail
          </h1>
          <p className="mt-4 max-w-3xl text-sm text-slate-300 sm:text-base">
            Review all serial number searches performed by your business branch,
            including timestamps and stolen matches.
          </p>

          {errorMessage && (
            <div className="mt-4 rounded-xl border border-red-400/30 bg-red-500/10 px-4 py-3 text-sm text-red-200">
              {errorMessage}
            </div>
          )}
        </section>

        <section className="mt-8 glass-panel rounded-2xl bg-slate-900/35 shadow-[0_8px_22px_rgba(0,0,0,0.2)]">
          <div className="border-b border-slate-800">
            <div className="flex gap-0 p-6 sm:p-8">
              <button
                onClick={() => setActiveTab("searches")}
                className={`flex items-center gap-2 border-b-2 px-4 py-2 text-sm font-semibold transition-colors -mb-6 ${
                  activeTab === "searches"
                    ? "border-brand text-brand"
                    : "border-transparent text-slate-400 hover:text-slate-200"
                }`}
              >
                <svg
                  className="h-5 w-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
                  />
                </svg>
                All Searches
              </button>
              <button
                onClick={() => setActiveTab("matches")}
                className={`flex items-center gap-2 border-b-2 px-4 py-2 text-sm font-semibold transition-colors -mb-6 ${
                  activeTab === "matches"
                    ? "border-red-500 text-red-400"
                    : "border-transparent text-slate-400 hover:text-slate-200"
                }`}
              >
                <svg
                  className="h-5 w-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                  />
                </svg>
                Stolen Matches
              </button>
            </div>
          </div>

          <div className="p-6 sm:p-8">
            {activeTab === "searches" && (
              <>
                {isLoading ? (
                  <div className="flex flex-col items-center justify-center p-12 opacity-60">
                    <svg
                      className="h-8 w-8 animate-spin text-brand"
                      viewBox="0 0 24 24"
                      fill="none"
                    >
                      <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                      ></circle>
                      <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                      ></path>
                    </svg>
                    <p className="mt-4 text-sm font-semibold tracking-wide text-brand">
                      Loading records...
                    </p>
                  </div>
                ) : history.length === 0 ? (
                  <div className="flex flex-col items-center justify-center p-12 text-slate-500">
                    <svg
                      className="h-12 w-12 opacity-50"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                      />
                    </svg>
                    <p className="mt-4 text-sm">
                      No searches found on this page.
                    </p>
                  </div>
                ) : (
                  <div className="overflow-hidden rounded-xl border border-slate-700/50 bg-slate-950/40">
                    <div className="overflow-x-auto">
                      <table className="min-w-full text-left text-sm">
                        <thead className="bg-slate-900/60 border-b border-slate-700/60 text-xs font-semibold uppercase tracking-wider text-slate-400">
                          <tr>
                            <th className="px-6 py-4">Item Serial</th>
                            <th className="px-6 py-4">Status</th>
                            <th className="px-6 py-4">Timestamp</th>
                            <th className="px-6 py-4 text-right">
                              Report Match
                            </th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-800/80">
                          {history.map((item, index) => (
                            <tr
                              key={`${item.timestamp}-${item.searchedSerial}-${index}`}
                              className="transition-colors hover:bg-slate-800/40"
                            >
                              <td className="whitespace-nowrap px-6 py-4 font-medium text-slate-200">
                                {item.searchedSerial}
                              </td>
                              <td className="whitespace-nowrap px-6 py-4">
                                <span
                                  className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-bold uppercase tracking-wide border ${
                                    item.result === "STOLEN"
                                      ? "border-red-500/20 bg-red-500/10 text-red-400"
                                      : "border-emerald-500/20 bg-emerald-500/10 text-emerald-400"
                                  }`}
                                >
                                  {item.result === "STOLEN" ? (
                                    <svg
                                      className="h-3 w-3"
                                      fill="none"
                                      viewBox="0 0 24 24"
                                      stroke="currentColor"
                                    >
                                      <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth={3}
                                        d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                                      />
                                    </svg>
                                  ) : (
                                    <svg
                                      className="h-3 w-3"
                                      fill="none"
                                      viewBox="0 0 24 24"
                                      stroke="currentColor"
                                    >
                                      <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth={3}
                                        d="M5 13l4 4L19 7"
                                      />
                                    </svg>
                                  )}
                                  {item.result}
                                </span>
                              </td>
                              <td className="whitespace-nowrap px-6 py-4 text-slate-400">
                                {formatDate(item.timestamp)}
                              </td>
                              <td className="whitespace-nowrap px-6 py-4 text-right text-slate-400">
                                {item.matchedReportId ? (
                                  <span className="rounded bg-slate-800 px-2 py-1 font-mono text-xs text-slate-300">
                                    #{item.matchedReportId}
                                  </span>
                                ) : (
                                  <span className="italic opacity-50">-</span>
                                )}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}
              </>
            )}

            {activeTab === "matches" && (
              <>
                {isLoading ? (
                  <div className="flex flex-col items-center justify-center p-12 opacity-60">
                    <svg
                      className="h-8 w-8 animate-spin text-red-400"
                      viewBox="0 0 24 24"
                      fill="none"
                    >
                      <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                      ></circle>
                      <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                      ></path>
                    </svg>
                    <p className="mt-4 text-sm font-semibold tracking-wide text-red-400">
                      Loading matches...
                    </p>
                  </div>
                ) : matches.length === 0 ? (
                  <div className="flex flex-col items-center justify-center p-12 text-slate-500">
                    <svg
                      className="h-12 w-12 opacity-50 text-emerald-500"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                      />
                    </svg>
                    <p className="mt-4 text-sm">
                      Great news! No stolen matches found on this page.
                    </p>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1.05fr_1.75fr] lg:items-start">
                    <aside className="rounded-2xl border border-red-500/20 bg-slate-900/40 p-4 shadow-xl flex flex-col lg:sticky lg:top-8 lg:max-h-[calc(100vh-8rem)]">
                      <h3 className="px-2 pb-3 text-sm font-semibold text-red-300 shrink-0">
                        Match Previews
                      </h3>
                      <div className="space-y-2.5 overflow-y-auto pr-2 lg:max-h-[calc(100vh-14rem)]">
                        {matches.map((item, index) => {
                          const isActive =
                            selectedMatch?.matchedReportId ===
                            item.matchedReportId;

                          return (
                            <button
                              key={`${item.timestamp}-${item.searchedSerial}-${index}`}
                              type="button"
                              onClick={() =>
                                setSelectedMatchId(item.matchedReportId ?? null)
                              }
                              className={`w-full rounded-xl border p-4 text-left transition-all duration-200 ${
                                isActive
                                  ? "border-red-400/50 bg-red-500/10"
                                  : "border-slate-700/60 bg-slate-900/40 hover:border-red-500/30 hover:bg-slate-800/50"
                              }`}
                            >
                              <div className="flex items-start justify-between gap-3">
                                <p className="font-mono text-sm font-semibold text-slate-100 truncate">
                                  {item.searchedSerial}
                                </p>
                                <span className="text-xs text-slate-400 whitespace-nowrap">
                                  {new Date(
                                    item.timestamp,
                                  ).toLocaleDateString()}
                                </span>
                              </div>
                              <p className="mt-1 text-xs text-red-300">
                                Report #{item.matchedReportId ?? "N/A"}
                              </p>
                              <p className="mt-2 text-xs text-slate-400 line-clamp-1">
                                {item.itemModel || "Unknown item model"}
                              </p>
                            </button>
                          );
                        })}
                      </div>
                    </aside>

                    {selectedMatch && (
                      <article className="rounded-2xl border border-red-500/20 bg-slate-900/40 p-6 sm:p-8 shadow-xl">
                        <div className="flex flex-wrap items-start justify-between gap-4 border-b border-red-500/20 pb-5">
                          <div>
                            <h3 className="text-2xl font-bold text-white">
                              Stolen Match Details
                            </h3>
                            <p className="mt-2 text-sm text-slate-400">
                              Review report owner details and evidence before
                              taking any action.
                            </p>
                          </div>
                          <span className="inline-flex items-center rounded-full border border-red-500/30 bg-red-500/10 px-3 py-1 text-xs font-bold uppercase tracking-wider text-red-300">
                            Report #{selectedMatch.matchedReportId ?? "N/A"}
                          </span>
                        </div>

                        <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2">
                          <div className="rounded-xl border border-slate-700/60 bg-slate-950/40 p-4">
                            <p className="text-xs uppercase tracking-[0.18em] text-slate-500">
                              Serial
                            </p>
                            <p className="mt-2 font-mono text-lg font-semibold text-slate-100">
                              {selectedMatch.searchedSerial}
                            </p>
                          </div>
                          <div className="rounded-xl border border-slate-700/60 bg-slate-950/40 p-4">
                            <p className="text-xs uppercase tracking-[0.18em] text-slate-500">
                              Matched At
                            </p>
                            <p className="mt-2 text-sm font-semibold text-slate-100">
                              {formatDate(selectedMatch.timestamp)}
                            </p>
                          </div>
                          <div className="rounded-xl border border-slate-700/60 bg-slate-950/40 p-4 sm:col-span-2">
                            <p className="text-xs uppercase tracking-[0.18em] text-slate-500">
                              Item Model
                            </p>
                            <p className="mt-2 text-sm font-semibold text-slate-100">
                              {selectedMatch.itemModel || "Unspecified Model"}
                            </p>
                          </div>
                          <div className="rounded-xl border border-slate-700/60 bg-slate-950/40 p-4 sm:col-span-2">
                            <p className="text-xs uppercase tracking-[0.18em] text-slate-500">
                              Description
                            </p>
                            <p className="mt-2 text-sm text-slate-300 whitespace-pre-wrap">
                              {selectedMatch.description ||
                                "No description provided."}
                            </p>
                          </div>
                        </div>

                        <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
                          <div className="rounded-xl border border-slate-700/60 bg-slate-950/40 p-4">
                            <p className="text-xs uppercase tracking-[0.18em] text-slate-500">
                              Owner Name
                            </p>
                            <p className="mt-2 text-sm font-semibold text-slate-100">
                              {selectedMatch.victimName || "Undisclosed"}
                            </p>
                          </div>
                          <div className="rounded-xl border border-slate-700/60 bg-slate-950/40 p-4">
                            <p className="text-xs uppercase tracking-[0.18em] text-slate-500">
                              Phone
                            </p>
                            <p className="mt-2 text-sm font-semibold text-slate-100">
                              {selectedMatch.victimPhoneNumber || "N/A"}
                            </p>
                          </div>
                          <div className="rounded-xl border border-slate-700/60 bg-slate-950/40 p-4">
                            <p className="text-xs uppercase tracking-[0.18em] text-slate-500">
                              Email
                            </p>
                            <p className="mt-2 text-sm font-semibold text-slate-100 break-all">
                              {selectedMatch.victimEmail || "N/A"}
                            </p>
                          </div>
                        </div>

                        <div className="mt-6 rounded-2xl border border-slate-700/50 bg-slate-950/40 p-5">
                          <div className="flex items-center justify-between gap-3">
                            <h4 className="text-sm font-semibold uppercase tracking-[0.15em] text-slate-300">
                              Uploaded Evidence
                            </h4>
                            {selectedMatch.evidenceFileUrl && (
                              <button
                                type="button"
                                onClick={() =>
                                  setViewerFile({
                                    url: selectedMatch.evidenceFileUrl!,
                                    type: inferEvidenceType(
                                      selectedMatch.evidenceFileType,
                                      selectedMatch.evidenceFileUrl,
                                    ),
                                  })
                                }
                                className="rounded-lg border border-brand/40 px-3 py-1.5 text-xs font-semibold text-brand hover:bg-brand/10"
                              >
                                Open Evidence
                              </button>
                            )}
                          </div>

                          {!selectedMatch.evidenceFileUrl ? (
                            <p className="mt-3 text-sm text-slate-500">
                              No evidence file was uploaded for this report.
                            </p>
                          ) : inferEvidenceType(
                              selectedMatch.evidenceFileType,
                              selectedMatch.evidenceFileUrl,
                            ) === "PDF" ? (
                            <div className="mt-4 rounded-xl border border-slate-700/60 bg-slate-900/50 p-4">
                              <p className="text-sm text-slate-300">
                                PDF evidence is available for review.
                              </p>
                            </div>
                          ) : (
                            <button
                              type="button"
                              onClick={() =>
                                setViewerFile({
                                  url: selectedMatch.evidenceFileUrl!,
                                  type: "IMAGE",
                                })
                              }
                              className="mt-4 block w-full overflow-hidden rounded-xl border border-slate-700/60"
                            >
                              <img
                                src={selectedMatch.evidenceFileUrl}
                                alt="Evidence preview"
                                className="h-56 w-full object-cover"
                              />
                            </button>
                          )}
                        </div>
                      </article>
                    )}
                  </div>
                )}
              </>
            )}
          </div>
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
