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
        setRecentReports(sorted.slice(0, 5));
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
                <p className="text-sm text-slate-400">You haven't reported any items yet.</p>
              </div>
            ) : (
              recentReports.map(report => {
                const imageFile = report.files.find(f => f.fileType === "IMAGE");
                const fileUrl = imageFile ? `${process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080"}${imageFile.fileUrl}` : null;
                return (
                  <Link key={report.id} href={`/reports?reportId=${report.id}`} className="group relative overflow-hidden rounded-3xl border border-white/5 bg-[#0a1628]/60 shadow-[0_8px_30px_rgba(0,0,0,0.2)] backdrop-blur-xl transition-all duration-300 hover:bg-[#0a1628]/80 hover:border-white/20 hover:-translate-y-1.5 hover:shadow-[0_20px_40px_rgba(0,0,0,0.4)] flex flex-col">
                    <div className="relative aspect-[4/3] w-full overflow-hidden bg-slate-800">
                      {imageFile && fileUrl ? (
                        <img src={fileUrl} alt={report.itemModel} className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-110" />
                      ) : (
                        <div className="flex h-full w-full items-center justify-center text-slate-600 bg-slate-900/50">
                          <svg className="h-10 w-10" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                          </svg>
                        </div>
                      )}
                      <div className="absolute inset-0 bg-gradient-to-t from-[#0a1628] via-[#0a1628]/20 to-transparent opacity-80" />
                    </div>
                    <div className="relative flex flex-col p-6 -mt-8">
                      <div className="mb-2 inline-flex self-start rounded-full bg-brand/20 px-2.5 py-0.5 text-[10px] font-bold uppercase tracking-wider text-brand backdrop-blur-md border border-brand/20 shadow-sm">
                        {new Date(report.createdAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })}
                      </div>
                      <h3 className="truncate text-lg font-bold text-white group-hover:text-brand transition-colors">{report.itemModel}</h3>
                      <p className="mt-2 inline-flex self-start text-xs text-slate-400 font-mono bg-white/5 rounded-md px-2 py-1 border border-white/5 shadow-inner">SN: {report.serialNumber}</p>
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


