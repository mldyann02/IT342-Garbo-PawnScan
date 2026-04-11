"use client";

import { useEffect, useMemo } from "react";
import { useRouter } from "next/navigation";
import { getAuthUser, getAuthRole, getJwt } from "@/lib/auth";
import BusinessDashboardHeader from "@/components/business-dashboard-header";

export default function SearchHistoryPage() {
  const router = useRouter();

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
    }
  }, [router, userRole]);

  return (
    <div className="min-h-screen bg-gradient-to-b from-bg-main to-[#071022] text-slate-200">
      <BusinessDashboardHeader />

      <main className="mx-auto w-full max-w-6xl px-4 pb-16 pt-36 sm:px-6 sm:pt-40 md:pt-32 lg:px-8">
        <section className="glass-panel rounded-2xl bg-slate-900/35 p-6 shadow-[0_18px_36px_rgba(0,0,0,0.28)] sm:p-8 flex flex-col items-center justify-center min-h-[400px]">
          <div className="text-center">
            <div className="mx-auto mb-6 inline-flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-brand/20 to-brand/5">
              <svg
                className="h-8 w-8 text-brand"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>

            <h1 className="text-3xl font-bold text-white sm:text-4xl">
              Search History
            </h1>
            <p className="mt-4 max-w-2xl text-base text-slate-300 sm:text-lg">
              Track all items you've verified through the PawnScan system and
              review verification results.
            </p>

            <div className="mt-8 inline-flex min-h-12 items-center gap-3 rounded-[10px] border border-border-muted bg-slate-950/70 px-4 py-3 text-slate-300">
              <svg
                aria-hidden="true"
                viewBox="0 0 24 24"
                className="h-5 w-5 text-brand animate-pulse"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <circle cx="12" cy="12" r="10" />
                <path d="M12 6v6l4 2" />
              </svg>
              <p className="text-sm">Coming Soon</p>
            </div>

            <p className="mt-6 text-sm text-slate-400">
              This feature is currently under development. Check back soon!
            </p>
          </div>
        </section>
      </main>
    </div>
  );
}
