"use client";

import Link from "next/link";
import { useEffect, useMemo } from "react";
import { useRouter } from "next/navigation";
import { getAuthUser, getAuthRole, getJwt } from "@/lib/auth";
import BusinessDashboardHeader from "@/components/business-dashboard-header";

export default function BusinessDashboardPage() {
  const router = useRouter();

  const userEmail = useMemo(() => {
    if (typeof window === "undefined") {
      return "";
    }

    return getAuthUser() || "";
  }, []);

  const userRole = useMemo(() => {
    if (typeof window === "undefined") {
      return "";
    }

    return getAuthRole() || "";
  }, []);

  useEffect(() => {
    const token = getJwt();
    const authenticatedEmail = getAuthUser();
    const role = getAuthRole();

    if (!token && !authenticatedEmail) {
      router.replace("/login");
      return;
    }

    // Redirect non-business users to regular dashboard
    if (role !== "BUSINESS") {
      router.replace("/dashboard");
    }
  }, [router]);

  return (
    <div className="min-h-screen bg-gradient-to-b from-bg-main to-[#071022] text-slate-200">
      <BusinessDashboardHeader />

      <main className="mx-auto w-full max-w-6xl px-4 pb-16 pt-36 sm:px-6 sm:pt-40 md:pt-32 lg:px-8">
        <section className="glass-panel rounded-2xl bg-slate-900/35 p-6 shadow-[0_18px_36px_rgba(0,0,0,0.28)] sm:p-8">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand/90">
            Business Dashboard
          </p>
          <h1 className="mt-2 text-3xl font-bold text-white sm:text-4xl">
            Protect your inventory and community.
          </h1>
          <p className="mt-4 max-w-2xl text-sm text-slate-300 sm:text-base">
            Verify incoming items against the PawnScan database, track your
            search history, and help keep stolen property out of your business.
          </p>
          <div className="mt-6 flex flex-wrap gap-3">
            <Link
              href="/business/verify"
              className="w-full rounded-full bg-brand px-5 py-2.5 text-center text-sm font-semibold text-bg-main transition-all duration-200 ease-out hover:brightness-90 active:scale-[0.98] sm:w-auto"
            >
              Verify Item
            </Link>
            <Link
              href="/business/search-history"
              className="w-full rounded-full bg-slate-800/65 px-5 py-2.5 text-center text-sm font-semibold text-brand transition-all duration-200 ease-out hover:bg-slate-700/75 active:scale-[0.98] sm:w-auto"
            >
              View Search History
            </Link>
          </div>
        </section>

        <section className="mt-6 grid grid-cols-1 gap-4 md:grid-cols-3">
          <article className="glass-panel rounded-xl bg-slate-900/45 p-5 shadow-[0_8px_22px_rgba(0,0,0,0.2)]">
            <p className="text-xs uppercase tracking-[0.18em] text-slate-400">
              Items Verified
            </p>
            <p className="mt-2 text-3xl font-bold text-white">42</p>
            <p className="mt-1 text-sm text-slate-300">
              Items verified this month.
            </p>
          </article>
          <article className="glass-panel rounded-xl bg-slate-900/45 p-5 shadow-[0_8px_22px_rgba(0,0,0,0.2)]">
            <p className="text-xs uppercase tracking-[0.18em] text-slate-400">
              Matches Found
            </p>
            <p className="mt-2 text-3xl font-bold text-white">3</p>
            <p className="mt-1 text-sm text-slate-300">
              Items matching stolen registry.
            </p>
          </article>
          <article className="glass-panel rounded-xl bg-slate-900/45 p-5 shadow-[0_8px_22px_rgba(0,0,0,0.2)]">
            <p className="text-xs uppercase tracking-[0.18em] text-slate-400">
              Account
            </p>
            <p className="mt-2 text-lg font-semibold text-white">
              {userRole === "ADMIN" ? "Admin" : "Business"}
            </p>
            <p className="mt-1 text-sm text-slate-300 break-all">
              {userEmail || "No user email found."}
            </p>
          </article>
        </section>
      </main>
    </div>
  );
}
