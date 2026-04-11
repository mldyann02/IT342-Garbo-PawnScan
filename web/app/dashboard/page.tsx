"use client";

import Link from "next/link";
import { useEffect, useMemo } from "react";
import { useRouter } from "next/navigation";
import { getAuthUser, getAuthRole, getJwt } from "@/lib/auth";
import UserDashboardHeader from "@/components/user-dashboard-header";

export default function DashboardPage() {
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

    // Redirect business users to business dashboard
    if (role === "BUSINESS") {
      router.replace("/business");
    }
  }, [router]);

  return (
    <div className="min-h-screen bg-gradient-to-b from-bg-main to-[#071022] text-slate-200">
      <UserDashboardHeader />

      <main className="mx-auto w-full max-w-6xl px-4 pb-16 pt-36 sm:px-6 sm:pt-40 md:pt-32 lg:px-8">
        <section className="glass-panel rounded-2xl bg-slate-900/35 p-6 shadow-[0_18px_36px_rgba(0,0,0,0.28)] sm:p-8">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand/90">
            User Dashboard
          </p>
          <h1 className="mt-2 text-3xl font-bold text-white sm:text-4xl">
            Keep your reported items visible and protected.
          </h1>
          <p className="mt-4 max-w-2xl text-sm text-slate-300 sm:text-base">
            Track your submitted reports, check updates from partner businesses,
            and quickly file new incident details when you need urgent help.
          </p>
          <div className="mt-6 flex flex-wrap gap-3">
            <Link
              href="/reports/create"
              className="w-full rounded-full bg-brand px-5 py-2.5 text-center text-sm font-semibold text-bg-main transition-all duration-200 ease-out hover:brightness-90 active:scale-[0.98] sm:w-auto"
            >
              Report New Item
            </Link>
            <Link
              href="/reports"
              className="w-full rounded-full bg-slate-800/65 px-5 py-2.5 text-center text-sm font-semibold text-brand transition-all duration-200 ease-out hover:bg-slate-700/75 active:scale-[0.98] sm:w-auto"
            >
              View My Reports
            </Link>
          </div>
        </section>

        <section className="mt-6 grid grid-cols-1 gap-4 md:grid-cols-3">
          <article className="glass-panel rounded-xl bg-slate-900/45 p-5 shadow-[0_8px_22px_rgba(0,0,0,0.2)]">
            <p className="text-xs uppercase tracking-[0.18em] text-slate-400">
              Active Reports
            </p>
            <p className="mt-2 text-3xl font-bold text-white">3</p>
            <p className="mt-1 text-sm text-slate-300">
              Cases currently under verification.
            </p>
          </article>
          <article className="glass-panel rounded-xl bg-slate-900/45 p-5 shadow-[0_8px_22px_rgba(0,0,0,0.2)]">
            <p className="text-xs uppercase tracking-[0.18em] text-slate-400">
              Matched Alerts
            </p>
            <p className="mt-2 text-3xl font-bold text-white">1</p>
            <p className="mt-1 text-sm text-slate-300">
              Item records similar to your reports.
            </p>
          </article>
          <article className="glass-panel rounded-xl bg-slate-900/45 p-5 shadow-[0_8px_22px_rgba(0,0,0,0.2)]">
            <p className="text-xs uppercase tracking-[0.18em] text-slate-400">
              Account
            </p>
            <p className="mt-2 text-lg font-semibold text-white">
              {userRole === "ADMIN" ? "Admin" : "User"}
            </p>
            <p className="mt-1 text-sm text-slate-300 break-all">
              {userEmail || "No user email found."}
            </p>
          </article>
        </section>

        <section className="mt-6 grid grid-cols-1 gap-4 lg:grid-cols-2">
          <article className="glass-panel rounded-xl bg-slate-900/45 p-6 shadow-[0_8px_22px_rgba(0,0,0,0.2)]">
            <h2 className="text-lg font-semibold text-white">
              Recent Activity
            </h2>
            <ul className="mt-4 space-y-3 text-sm text-slate-300">
              <li className="rounded-lg bg-slate-900/45 px-4 py-3">
                April 10: Report PS-1102 was updated with new serial details.
              </li>
              <li className="rounded-lg bg-slate-900/45 px-4 py-3">
                April 9: One business checked your bicycle report.
              </li>
              <li className="rounded-lg bg-slate-900/45 px-4 py-3">
                April 8: Account verification completed successfully.
              </li>
            </ul>
          </article>

          <article className="glass-panel rounded-xl bg-slate-900/45 p-6 shadow-[0_8px_22px_rgba(0,0,0,0.2)]">
            <h2 className="text-lg font-semibold text-white">
              Tips For Better Reports
            </h2>
            <ul className="mt-4 space-y-3 text-sm text-slate-300">
              <li className="rounded-lg bg-slate-900/45 px-4 py-3">
                Include serial numbers and close-up item photos whenever
                possible.
              </li>
              <li className="rounded-lg bg-slate-900/45 px-4 py-3">
                Add the last known location and exact date to improve matching.
              </li>
              <li className="rounded-lg bg-slate-900/45 px-4 py-3">
                Keep contact details updated so investigators can reach you
                quickly.
              </li>
            </ul>
          </article>
        </section>
      </main>
    </div>
  );
}
