"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthRole, getJwt } from "@/shared/auth";
import { AdminStats, fetchAdminStats } from "@/features/admin/lib/admin";
import Link from "next/link";

export default function AdminDashboardPage() {
  const router = useRouter();
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = getJwt();
    const role = getAuthRole();

    if (!token || role !== "ADMIN") {
      router.replace("/login");
      return;
    }

    fetchAdminStats()
      .then((data) => setStats(data))
      .catch(console.error)
      .finally(() => setIsLoading(false));
  }, [router]);

  return (
    <div className="min-h-screen text-slate-200">
      <main className="mx-auto w-full max-w-5xl px-4 pb-16 pt-32 sm:px-6 sm:pt-36 lg:px-8">
        {/* Hero Section */}
        <section className="relative overflow-hidden rounded-3xl border-0 bg-[#0a1628]/80 p-8 sm:p-12 shadow-[0_24px_60px_rgba(0,0,0,0.4)] backdrop-blur-xl">
          <div className="absolute top-0 right-0 -mr-20 -mt-20 h-72 w-72 rounded-full bg-brand/10 blur-[80px] pointer-events-none" />

          <div className="relative z-10 max-w-2xl">
            <h1 className="text-4xl font-extrabold tracking-tight text-transparent bg-clip-text bg-gradient-to-br from-white via-slate-200 to-brand sm:text-5xl pb-1">
              Admin Overview
            </h1>

            <p className="mt-4 text-lg text-slate-400 font-light leading-relaxed max-w-xl">
              Monitor system integrity, moderate user reports, and manage business verifications all from one place.
            </p>
          </div>
        </section>

        {/* Stats Grid */}
        <section className="mt-12">
          <h2 className="text-2xl font-extrabold text-white tracking-tight mb-8">System Integrity Metrics</h2>
          
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
            {isLoading ? (
              <div className="col-span-full py-12 flex flex-col items-center justify-center">
                 <div className="w-8 h-8 border-2 border-brand/30 border-t-brand rounded-full animate-spin mb-4" />
                 <p className="text-sm text-slate-400">Loading metrics...</p>
              </div>
            ) : (
              <>
                <div className="relative overflow-hidden rounded-3xl border-0 bg-[#0a1628]/60 p-6 shadow-[0_8px_30px_rgba(0,0,0,0.2)] backdrop-blur-xl">
                  <p className="text-sm font-medium text-slate-400">Total Users</p>
                  <p className="mt-2 text-4xl font-bold text-white">{stats?.totalUsers || 0}</p>
                </div>
                
                <div className="relative overflow-hidden rounded-3xl border-0 bg-[#0a1628]/60 p-6 shadow-[0_8px_30px_rgba(0,0,0,0.2)] backdrop-blur-xl">
                  <p className="text-sm font-medium text-slate-400">Total Businesses</p>
                  <p className="mt-2 text-4xl font-bold text-white">{stats?.totalBusinesses || 0}</p>
                </div>

                <div className="relative overflow-hidden rounded-3xl border-0 bg-[#0a1628]/60 p-6 shadow-[0_8px_30px_rgba(0,0,0,0.2)] backdrop-blur-xl">
                  <p className="text-sm font-medium text-brand">Pending Reports</p>
                  <p className="mt-2 text-4xl font-bold text-white">{stats?.pendingReports || 0}</p>
                  <Link href="/admin/moderation" className="absolute inset-0 z-10" aria-label="View Pending Reports" />
                </div>

                <div className="relative overflow-hidden rounded-3xl border-0 bg-[#0a1628]/60 p-6 shadow-[0_8px_30px_rgba(0,0,0,0.2)] backdrop-blur-xl">
                  <p className="text-sm font-medium text-brand">Pending Verification</p>
                  <p className="mt-2 text-4xl font-bold text-white">{stats?.pendingBusinesses || 0}</p>
                  <Link href="/admin/users" className="absolute inset-0 z-10" aria-label="View Pending Businesses" />
                </div>
              </>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}
