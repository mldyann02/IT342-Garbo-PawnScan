"use client";

import { useEffect, useState } from "react";
import { fetchMe, getAuthRole, clearAuthSession } from "@/shared/auth";
import { invalidateReportsCache } from "@/features/reports/lib/reports";
import Link from "next/link";
import { useRouter } from "next/navigation";

export default function VerificationGuard({ children }: { children: React.ReactNode }) {
  const [isVerified, setIsVerified] = useState<boolean | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    async function checkVerification() {
      const role = getAuthRole();
      if (role !== "BUSINESS") {
        setLoading(false);
        return;
      }

      const me = await fetchMe();
      if (me && me.businessProfile) {
        // Jackson serializes boolean 'isVerified' as 'verified'
        const isVerifiedStatus = me.businessProfile.verified !== undefined 
          ? me.businessProfile.verified 
          : me.businessProfile.isVerified;
        setIsVerified(!!isVerifiedStatus);
      } else {
        // Fallback or error fetching profile
        setIsVerified(false);
      }
      setLoading(false);
    }
    checkVerification();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center text-slate-200">
        <div className="flex flex-col items-center">
          <div className="w-8 h-8 border-2 border-brand/30 border-t-brand rounded-full animate-spin mb-4" />
          <p className="text-sm text-slate-400">Loading your profile...</p>
        </div>
      </div>
    );
  }

  // If verified or not a business user, render children normally
  if (isVerified === true || isVerified === null) {
    return <>{children}</>;
  }

  // Unverified State UI
  return (
    <div className="min-h-screen relative overflow-hidden text-slate-200 flex flex-col pt-32 px-4">
      <div className="absolute inset-0 z-0 pointer-events-none">
        <div className="absolute top-1/3 right-1/4 w-[30rem] h-[30rem] bg-amber-500/10 rounded-full blur-[150px]" />
        <div className="absolute bottom-1/4 left-1/4 w-96 h-96 bg-brand/5 rounded-full blur-[120px]" />
      </div>

      <main className="relative z-10 mx-auto w-full max-w-2xl text-center">
        <div className="bg-[#0a1628]/80 backdrop-blur-xl border border-white/5 rounded-3xl p-8 sm:p-12 shadow-2xl flex flex-col items-center">
          <div className="w-20 h-20 rounded-full bg-amber-500/10 border border-amber-500/20 flex items-center justify-center mb-6">
            <svg className="w-10 h-10 text-amber-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          
          <h1 className="text-3xl font-extrabold tracking-tight text-white mb-4">
            Pending Admin Approval
          </h1>
          <p className="text-lg text-slate-400 leading-relaxed max-w-lg mb-8">
            Your business account is currently under review. Our administrators are verifying your provided credentials and permit number. You will gain full access to the business portal once approved.
          </p>
          
          <div className="flex flex-col sm:flex-row gap-4 w-full sm:w-auto">
            <button 
              onClick={() => window.location.reload()} 
              className="inline-flex items-center justify-center rounded-xl bg-brand px-6 py-3 text-sm font-bold text-slate-900 shadow-lg shadow-brand/20 transition-all hover:scale-105 hover:bg-brand/80"
            >
              Refresh Status
            </button>
            <button 
              onClick={() => {
                clearAuthSession();
                invalidateReportsCache();
                router.push("/login");
              }}
              className="inline-flex items-center justify-center rounded-xl bg-slate-800 px-6 py-3 text-sm font-bold text-white shadow-lg shadow-slate-900/20 transition-all hover:scale-105 hover:bg-slate-700"
            >
              Logout
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}
