"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthUser, getAuthRole, getJwt } from "@/shared/auth";
import { fetchSearchHistory, SearchLog } from "@/features/verification/lib/verify";

export default function BusinessDashboardPage() {
  const router = useRouter();
  const [recentSearches, setRecentSearches] = useState<SearchLog[]>([]);
  const [isLoadingSearches, setIsLoadingSearches] = useState(true);

  const userEmail = useMemo(() => {
    if (typeof window === "undefined") return "";
    return getAuthUser() || "";
  }, []);

  const userRole = useMemo(() => {
    if (typeof window === "undefined") return "";
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

    if (role !== "BUSINESS") {
      router.replace("/dashboard");
      return;
    }

    fetchSearchHistory(0, 6)
      .then(data => setRecentSearches(data))
      .catch(console.error)
      .finally(() => setIsLoadingSearches(false));
  }, [router]);

  return (
    <div className="min-h-screen relative overflow-hidden text-slate-200">
      {/* Decorative blurred background */}
      <div className="absolute inset-0 z-0 pointer-events-none">
        <div className="absolute top-1/3 right-1/4 w-[30rem] h-[30rem] bg-brand/10 rounded-full blur-[150px]" />
        <div className="absolute bottom-1/4 left-1/4 w-96 h-96 bg-brand/5 rounded-full blur-[120px]" />
      </div>

      <main className="relative z-10 mx-auto w-full max-w-5xl px-4 pb-16 pt-32 sm:px-6 lg:px-8">
        
        {/* Welcome Section */}
        <section className="bg-[#0a1628]/80 backdrop-blur-xl border border-white/5 rounded-3xl p-8 sm:p-12 shadow-2xl relative overflow-hidden">
          <div className="relative z-10 flex flex-col md:flex-row md:items-end justify-between gap-8">
            <div className="max-w-2xl">
              <div className="inline-flex items-center gap-2 rounded-full border border-brand/20 bg-brand/10 px-3 py-1 mb-6 text-sm font-medium text-brand">
                <span className="relative flex h-2 w-2">
                  <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-brand opacity-75"></span>
                  <span className="relative inline-flex h-2 w-2 rounded-full bg-brand"></span>
                </span>
                Business Portal
              </div>
              <h1 className="text-4xl font-extrabold tracking-tight text-white sm:text-5xl mb-4">
                Verify items instantly.
              </h1>
              <p className="text-lg text-slate-400 leading-relaxed">
                Check incoming items against our stolen registry to protect your inventory and ensure secure transactions.
              </p>
            </div>
            
            <div className="flex shrink-0">
              <Link
                href="/business/verify"
                className="group relative inline-flex items-center justify-center gap-3 overflow-hidden rounded-full bg-brand px-8 py-4 text-base font-bold text-bg-main transition-all duration-300 hover:scale-105 hover:shadow-[0_0_40px_rgba(var(--color-brand),0.4)]"
              >
                <svg className="w-5 h-5 transition-transform group-hover:rotate-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M12 4v16m8-8H4" />
                </svg>
                Verify Item
                <div className="absolute inset-0 bg-white/20 translate-y-full transition-transform duration-300 group-hover:translate-y-0" />
              </Link>
            </div>
          </div>
        </section>

        {/* Recent Verifications */}
        <section className="mt-12">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl font-extrabold text-white tracking-tight">Recent Verifications</h2>
            <Link href="/business/search-history" className="group flex items-center gap-2 text-sm font-semibold text-brand hover:text-brand/80 transition-colors">
              View All History 
              <span className="transition-transform duration-300 group-hover:translate-x-1">&rarr;</span>
            </Link>
          </div>
          
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {isLoadingSearches ? (
              <div className="col-span-full py-12 flex flex-col items-center justify-center">
                 <div className="w-8 h-8 border-2 border-brand/30 border-t-brand rounded-full animate-spin mb-4" />
                 <p className="text-sm text-slate-400">Loading your history...</p>
              </div>
            ) : recentSearches.length === 0 ? (
              <div className="col-span-full py-16 text-center border border-dashed border-white/10 rounded-3xl bg-white/5 backdrop-blur-sm">
                <div className="mx-auto w-12 h-12 text-slate-600 mb-4 flex items-center justify-center">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" className="w-8 h-8"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
                </div>
                <h3 className="text-lg font-bold text-white mb-2">No verifications yet</h3>
                <p className="text-sm text-slate-400 mb-6">You haven&apos;t scanned any items today.</p>
                <Link href="/business/verify" className="inline-flex items-center justify-center rounded-xl bg-slate-800 px-6 py-2.5 text-sm font-semibold text-white transition-all hover:bg-slate-700">
                  Start Scanning
                </Link>
              </div>
            ) : (
              recentSearches.map((search, idx) => {
                const isStolen = search.result === "STOLEN";
                return (
                  <div key={idx} className={`group relative overflow-hidden rounded-3xl border bg-[#0a1628]/80 p-6 shadow-[0_8px_30px_rgba(0,0,0,0.2)] backdrop-blur-xl transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_20px_40px_rgba(0,0,0,0.4)] ${isStolen ? 'border-transparent' : 'border-white/5 hover:border-status-clean/30'}`}>
                    <div className={`absolute inset-0 opacity-20 transition-opacity duration-300 group-hover:opacity-40 bg-gradient-to-br ${isStolen ? 'from-transparent to-status-stolen/30' : 'from-transparent to-status-clean/20'}`} />
                    
                    <div className="relative z-10 flex flex-col h-full">
                      <div className="flex items-center justify-between mb-5">
                        <div className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1.5 text-[11px] font-extrabold uppercase tracking-widest border shadow-sm ${isStolen ? 'border-status-stolen/40 bg-status-stolen/10 text-status-stolen shadow-status-stolen/20' : 'border-status-clean/30 bg-status-clean/10 text-status-clean shadow-status-clean/10'}`}>
                          {isStolen ? (
                            <>
                              <span className="relative flex h-2 w-2 mr-0.5">
                                <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-status-stolen opacity-75"></span>
                                <span className="relative inline-flex h-2 w-2 rounded-full bg-status-stolen"></span>
                              </span>
                              Stolen Match
                            </>
                          ) : (
                            <>
                              <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7"/></svg>
                              Clean
                            </>
                          )}
                        </div>
                        <span className="text-xs text-slate-500 font-medium bg-slate-900/50 px-2 py-1 rounded-md border border-slate-800">
                          {new Date(search.timestamp).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
                        </span>
                      </div>
                      
                      <div className="mt-auto">
                        <p className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1.5">Searched Serial</p>
                        <p className="text-2xl font-black text-white tracking-tight truncate font-mono bg-slate-900/40 p-2.5 rounded-xl border border-slate-800/60 shadow-inner group-hover:text-brand transition-colors duration-300">{search.searchedSerial}</p>
                      </div>

                      {/* Hover Overlay for Stolen Matches */}
                      {isStolen && search.matchedReportId && (
                        <div className="absolute inset-0 z-20 flex items-center justify-center bg-[#0a1628]/60 backdrop-blur-[4px] opacity-0 transition-opacity duration-300 group-hover:opacity-100 rounded-3xl m-[-1.5rem]">
                          <Link href={`/business/search-history?tab=matches&matchId=${search.matchedReportId}`} className="inline-flex items-center gap-2 rounded-full bg-status-stolen px-5 py-2.5 text-sm font-bold text-white shadow-[0_0_20px_rgba(239,68,68,0.4)] transition-transform hover:scale-105">
                            View Details
                            <span className="transition-transform duration-300 group-hover:translate-x-1">&rarr;</span>
                          </Link>
                        </div>
                      )}
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </section>

      </main>
    </div>
  );
}


