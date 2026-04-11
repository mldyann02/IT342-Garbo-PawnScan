"use client";

import Link from "next/link";
import { useEffect } from "react";
import { useRouter } from "next/navigation";
import UserDashboardHeader from "@/components/user-dashboard-header";
import { getAuthUser, getJwt } from "@/lib/auth";

const MOCK_REPORTS = [
  {
    id: "PS-1102",
    item: "Mountain Bike",
    dateReported: "Apr 10, 2026",
    status: "Under Review",
    location: "Cebu City",
  },
  {
    id: "PS-1097",
    item: "Laptop - 14 inch",
    dateReported: "Apr 05, 2026",
    status: "Potential Match",
    location: "Mandaue City",
  },
  {
    id: "PS-1084",
    item: "Gold Bracelet",
    dateReported: "Mar 29, 2026",
    status: "Open",
    location: "Talisay City",
  },
];

export default function ReportsPage() {
  const router = useRouter();

  useEffect(() => {
    const token = getJwt();
    const authenticatedEmail = getAuthUser();

    if (!token && !authenticatedEmail) {
      router.replace("/login");
    }
  }, [router]);

  return (
    <div className="min-h-screen bg-gradient-to-b from-bg-main to-[#071022] text-slate-200">
      <UserDashboardHeader />

      <main className="mx-auto w-full max-w-6xl px-4 pb-16 pt-36 sm:px-6 sm:pt-40 md:pt-32 lg:px-8">
        <section className="glass-panel rounded-2xl bg-slate-900/35 p-6 shadow-[0_18px_36px_rgba(0,0,0,0.28)] sm:p-8">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand/90">
                My Reports
              </p>
              <h1 className="mt-2 text-2xl font-bold text-white sm:text-3xl">Your submitted reports</h1>
              <p className="mt-3 text-sm text-slate-300">
                Monitor verification progress and review status updates for your
                stolen item submissions.
              </p>
            </div>

            <Link
              href="/reports/create"
              className="w-full rounded-full bg-brand px-5 py-2.5 text-center text-sm font-semibold text-bg-main transition-all duration-200 ease-out hover:brightness-90 active:scale-[0.98] sm:w-auto"
            >
              New Report
            </Link>
          </div>

          <div className="mt-6 space-y-3 md:hidden">
            {MOCK_REPORTS.map((report) => (
              <article
                key={`mobile-${report.id}`}
                className="rounded-xl bg-slate-900/60 p-4 text-sm text-slate-200 shadow-[0_6px_18px_rgba(0,0,0,0.18)]"
              >
                <div className="flex items-start justify-between gap-3">
                  <p className="font-semibold text-brand">{report.id}</p>
                  <span className="inline-flex rounded-full bg-brand/20 px-3 py-1 text-xs font-semibold text-brand">
                    {report.status}
                  </span>
                </div>
                <p className="mt-2 text-base font-semibold text-white">{report.item}</p>
                <p className="mt-1 text-slate-300">{report.location}</p>
                <p className="mt-1 text-xs uppercase tracking-[0.14em] text-slate-400">
                  Reported {report.dateReported}
                </p>
              </article>
            ))}
          </div>

          <div className="mt-6 hidden overflow-x-auto hide-scrollbar md:block">
            <table className="w-full min-w-[680px] border-separate border-spacing-y-3">
              <thead>
                <tr className="text-left text-xs uppercase tracking-[0.15em] text-slate-400">
                  <th className="px-3 py-2">Reference</th>
                  <th className="px-3 py-2">Item</th>
                  <th className="px-3 py-2">Date Reported</th>
                  <th className="px-3 py-2">Location</th>
                  <th className="px-3 py-2">Status</th>
                </tr>
              </thead>
              <tbody>
                {MOCK_REPORTS.map((report) => (
                  <tr
                    key={report.id}
                    className="rounded-xl bg-slate-900/60 text-sm text-slate-200 shadow-[0_6px_18px_rgba(0,0,0,0.18)] transition-all duration-200 ease-out hover:bg-slate-800/70"
                  >
                    <td className="rounded-l-lg px-3 py-3 font-semibold text-brand">
                      {report.id}
                    </td>
                    <td className="px-3 py-3">{report.item}</td>
                    <td className="px-3 py-3">{report.dateReported}</td>
                    <td className="px-3 py-3">{report.location}</td>
                    <td className="rounded-r-lg px-3 py-3">
                      <span className="inline-flex rounded-full bg-brand/20 px-3 py-1 text-xs font-semibold text-brand">
                        {report.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  );
}