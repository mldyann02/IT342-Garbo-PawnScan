"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthUser, getAuthRole, getJwt } from "@/shared/auth";
import { fetchReports, Report } from "@/features/reports/lib/reports";

export default function DashboardPage() {
  const router = useRouter();
  const [recentReports, setRecentReports] = useState<Report[]>([]);
  const [isLoadingReports, setIsLoadingReports] = useState(true);

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
      return;
    }

      fetchReports()
      .then((data) => {
        const sorted = [...data].sort(
          (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        setRecentReports(sorted.slice(0, 6));
      })
      .catch(console.error)
      .finally(() => setIsLoadingReports(false));
  }, [router]);

  return (
    <div className="min-h-screen text-slate-200">
      <main className="mx-auto w-full max-w-5xl px-4 pb-16 pt-32 sm:px-6 sm:pt-36 lg:px-8">

        {/* Hero Section */}
        <section className="relative overflow-hidden rounded-3xl bg-[#0a1628]/80 border border-white/5 p-8 sm:p-12 shadow-[0_24px_60px_rgba(0,0,0,0.3)] backdrop-blur-xl">
          <div className="absolute top-0 right-0 -mr-20 -mt-20 h-72 w-72 rounded-full bg-brand/10 blur-[80px] pointer-events-none" />

          <div className="relative z-10 max-w-2xl">
            <h1 className="text-4xl font-extrabold tracking-tight text-transparent bg-clip-text bg-gradient-to-br from-white via-slate-200 to-slate-500 sm:text-5xl pb-1">
              Keep your items protected.
            </h1>

            <p className="mt-4 text-lg text-slate-400 font-light leading-relaxed max-w-xl">
              Track your submitted reports, monitor updates from partner businesses, and quickly file new incident details when you need urgent help.
            </p>

            <div className="mt-8 flex flex-wrap gap-4">
              <Link
                href="/reports/create"
                className="inline-flex items-center justify-center rounded-xl bg-brand px-6 py-3 text-sm font-bold text-slate-950 shadow-lg shadow-brand/20 transition-all duration-300 hover:shadow-brand/40 hover:-translate-y-0.5 hover:brightness-110 active:scale-95"
              >
                Report New Item
              </Link>
              <Link
                href="/reports"
                className="inline-flex items-center justify-center rounded-xl border border-slate-700/50 bg-slate-900/50 px-6 py-3 text-sm font-semibold text-slate-200 backdrop-blur-sm transition-all duration-300 hover:bg-slate-800/80 hover:text-white hover:border-slate-600 active:scale-95"
              >
                View My Reports
              </Link>
            </div>
          </div>
        </section>



        {/* Recent Reports */}
        <section className="mt-12">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl font-extrabold text-white tracking-tight">Recent Reports</h2>
            <Link href="/reports" className="group flex items-center gap-2 text-sm font-semibold text-brand hover:text-brand/80 transition-colors">
              View All 
              <span className="transition-transform duration-300 group-hover:translate-x-1">&rarr;</span>
            </Link>
          </div>
          
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {isLoadingReports ? (
              <div className="col-span-full py-12 flex flex-col items-center justify-center">
                 <div className="w-8 h-8 border-2 border-brand/30 border-t-brand rounded-full animate-spin mb-4" />
                 <p className="text-sm text-slate-400">Loading recent reports...</p>
              </div>
            ) : recentReports.length === 0 ? (
              <div className="col-span-full py-16 text-center border border-dashed border-white/10 rounded-3xl bg-white/5 backdrop-blur-sm">
                <div className="mx-auto w-12 h-12 text-slate-600 mb-4 flex items-center justify-center">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" className="w-8 h-8"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
                </div>
                <h3 className="text-lg font-bold text-white mb-2">No reports found</h3>
                <p className="text-sm text-slate-400">You haven&apos;t reported any items yet.</p>
              </div>
            ) : (
              recentReports.map(report => {
                return (
                  <Link key={report.id} href={`/reports?reportId=${report.id}`} className="group relative overflow-hidden rounded-3xl border border-white/5 bg-gradient-to-b from-[#0f1f38] to-[#0a1628] p-6 shadow-[0_8px_30px_rgba(0,0,0,0.2)] backdrop-blur-xl transition-all duration-300 hover:border-brand/30 hover:shadow-[0_20px_40px_rgba(0,0,0,0.4)] hover:-translate-y-1.5 flex flex-col min-h-[160px]">
                    <div className="absolute top-0 right-0 -mr-8 -mt-8 h-32 w-32 rounded-full bg-brand/5 blur-2xl transition-all duration-500 group-hover:bg-brand/15" />
                    
                    <div className="flex justify-between items-start mb-4 relative z-10">
                      <div className="inline-flex items-center justify-center rounded-xl bg-slate-800/80 border border-white/10 p-2.5 text-brand shadow-inner group-hover:scale-110 group-hover:bg-brand/10 group-hover:border-brand/20 transition-all duration-300">
                        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                      </div>
                      <div className="inline-flex rounded-full bg-slate-800/50 px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider text-slate-300 border border-white/5">
                        {new Date(report.createdAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })}
                      </div>
                    </div>
                    
                    <div className="relative z-10 mt-auto flex flex-col gap-2">
                      <h3 className="truncate text-lg font-bold text-white group-hover:text-brand transition-colors">{report.itemModel}</h3>
                      <div className="flex items-center gap-2">
                        <span className="inline-flex items-center text-xs text-slate-400 font-mono bg-black/20 rounded-md px-2 py-1 border border-white/5">
                          <span className="text-slate-500 mr-1">SN:</span> {report.serialNumber}
                        </span>
                      </div>
                    </div>
                  </Link>
                );
              })
            )}
          </div>
        </section>

      </main>
    </div>
  );
}


