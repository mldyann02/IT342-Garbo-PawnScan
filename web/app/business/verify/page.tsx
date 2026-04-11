"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthUser, getAuthRole, getJwt } from "@/lib/auth";
import BusinessDashboardHeader from "@/components/business-dashboard-header";
import {
  VerifySearchResponse,
  fetchSearchHistory,
  verifySerialNumber,
} from "@/lib/verify";

function formatDate(value: string): string {
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleString();
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

    async function loadRecentSearches() {
      try {
        const history = await fetchSearchHistory(0, 5);
        setRecentSearches(history);
      } catch {
        setRecentSearches([]);
      }
    }

    loadRecentSearches();
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

      const updatedHistory = await fetchSearchHistory(0, 5);
      setRecentSearches(updatedHistory);
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "Verification failed.",
      );
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-bg-main to-[#071022] text-slate-200">
      <BusinessDashboardHeader />

      <main className="mx-auto w-full max-w-6xl px-4 pb-16 pt-36 sm:px-6 sm:pt-40 md:pt-32 lg:px-8">
        <section className="glass-panel rounded-2xl bg-slate-900/35 p-6 shadow-[0_18px_36px_rgba(0,0,0,0.28)] sm:p-8">
          <h1 className="mt-2 text-3xl font-bold text-white sm:text-4xl">
            Verify if an item is stolen
          </h1>
          <p className="mt-4 max-w-3xl text-sm text-slate-300 sm:text-base">
            Enter a serial number to check PawnScan approved stolen records.
            Every lookup is logged automatically to your business history.
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
              className={`mt-8 overflow-hidden rounded-2xl border bg-slate-900/55 p-6 shadow-lg ${
                result.status === "STOLEN"
                  ? "border-red-500/30"
                  : "border-emerald-500/30"
              }`}
            >
              <div className="flex flex-col items-center justify-between gap-4 text-center sm:flex-row sm:text-left">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">
                    Verification Result
                  </p>
                  <p className="mt-1 text-lg text-slate-200">
                    Serial searched:{" "}
                    <span className="font-bold text-white">
                      {result.serial}
                    </span>
                  </p>
                </div>
                <span
                  className={`rounded-full border px-4 py-1.5 text-xs font-bold tracking-[0.15em] ${
                    result.status === "STOLEN"
                      ? "border-red-500/40 bg-red-500/20 text-red-300"
                      : "border-emerald-500/40 bg-emerald-500/20 text-emerald-300"
                  }`}
                >
                  {result.status}
                </span>
              </div>

              {result.status === "STOLEN" && result.report && (
                <div className="mt-6 rounded-xl border border-red-500/20 bg-red-500/10 p-5">
                  <h3 className="mb-4 text-sm font-bold uppercase tracking-wider text-red-300">
                    Stolen Report Details
                  </h3>
                  <div className="grid gap-4 sm:grid-cols-2">
                    <p className="text-sm text-slate-200">
                      Report ID:{" "}
                      <span className="font-semibold">
                        #{result.report.reportId}
                      </span>
                    </p>
                    <p className="text-sm text-slate-200">
                      Date Reported:{" "}
                      <span className="font-semibold">
                        {formatDate(result.report.dateReported)}
                      </span>
                    </p>
                    <p className="text-sm text-slate-200 sm:col-span-2">
                      Item Model:{" "}
                      <span className="font-semibold">
                        {result.report.itemModel}
                      </span>
                    </p>
                    <p className="text-sm text-slate-200 sm:col-span-2">
                      Description:{" "}
                      <span className="font-semibold">
                        {result.report.description}
                      </span>
                    </p>
                  </div>
                </div>
              )}
            </div>
          )}
        </section>

        <section className="mt-8 glass-panel rounded-2xl bg-slate-900/35 p-6 shadow-[0_8px_22px_rgba(0,0,0,0.2)] sm:p-8">
          <div className="flex items-center justify-between gap-3 border-b border-slate-800 pb-4">
            <h2 className="text-lg font-bold text-white">Recent Searches</h2>
            <button
              type="button"
              onClick={async () => {
                try {
                  const history = await fetchSearchHistory(0, 5);
                  setRecentSearches(history);
                } catch {
                  setRecentSearches([]);
                }
              }}
              className="rounded-lg border border-slate-600/70 px-3 py-1.5 text-xs font-semibold text-slate-200 transition hover:bg-slate-800/70"
            >
              Refresh
            </button>
          </div>

          {recentSearches.length === 0 ? (
            <p className="mt-4 text-sm text-slate-400">
              No recent searches yet.
            </p>
          ) : (
            <ul className="mt-4 space-y-2">
              {recentSearches.map((item, index) => (
                <li
                  key={`${item.timestamp}-${item.searchedSerial}-${index}`}
                  className="flex flex-wrap items-center justify-between gap-2 rounded-lg bg-slate-900/50 px-3 py-2 text-sm"
                >
                  <span className="text-slate-200">{item.searchedSerial}</span>
                  <span
                    className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                      item.result === "STOLEN"
                        ? "bg-red-500/15 text-red-300"
                        : "bg-emerald-500/15 text-emerald-300"
                    }`}
                  >
                    {item.result}
                  </span>
                  <span className="text-xs text-slate-400">
                    {formatDate(item.timestamp)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>
    </div>
  );
}
