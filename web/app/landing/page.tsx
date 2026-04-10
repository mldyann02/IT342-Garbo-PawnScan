"use client";

import { useRouter } from "next/navigation";
import Header from "@/components/header";

export default function LandingPage() {
  const router = useRouter();

  const handleReportClick = () => {
    router.push("/register?role=INDIVIDUAL");
  };

  const handleBusinessClick = () => {
    router.push("/register?role=BUSINESS");
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-bg-main to-[#071022] text-slate-200">
      {/* Header */}
      <Header />

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-6 pb-16 pt-32 md:pb-24 md:pt-36">
        {/* Badge */}
        <div className="flex justify-center mb-8">
          <div className="inline-flex items-center gap-2 px-4 py-2 bg-green-900/20 border border-brand/30 rounded-full">
            <svg
              className="w-4 h-4 text-brand"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path d="M9.5 2a1 1 0 011 1v1.057a7.002 7.002 0 015.192 10.876 1 1 0 11-1.414-1.414 5.002 5.002 0 10-3.71 8.247 1 1 0 11-.868 1.806A7.002 7.002 0 015.507 9.879V8.5a1 1 0 011-1h3a1 1 0 011 1v.179a1 1 0 11-2 0V8.5H7.5a1 1 0 11-2 0V7.443A7.002 7.002 0 019.5 2z" />
            </svg>
            <span className="text-brand text-xs font-bold uppercase tracking-widest">
              Real-time Stolen Item Verification
            </span>
          </div>
        </div>

        {/* Headline */}
        <div className="text-center mb-6">
          <h1 className="text-5xl md:text-6xl font-bold leading-tight mb-3">
            <span className="text-white">Stop stolen goods</span>
            <br />
            <span className="text-white">before they</span>
            <br />
            <span className="text-brand">sell.</span>
          </h1>
        </div>

        {/* Subheadline */}
        <p className="text-center text-slate-400 text-lg md:text-xl max-w-2xl mx-auto mb-12">
          Connect victims, businesses, and law enforcement to prevent stolen
          items from entering the market. Report, verify, and protect in
          real-time.
        </p>

        {/* CTA Buttons */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center max-w-xl mx-auto">
          <button
            onClick={handleReportClick}
            className="flex-1 px-8 py-4 bg-brand text-bg-main font-bold rounded-full hover:brightness-90 active:scale-95 active:brightness-85 transition-all text-center text-lg"
          >
            Report a Stolen Item
          </button>
          <button
            onClick={handleBusinessClick}
            className="flex-1 px-8 py-4 bg-transparent border-2 border-brand text-brand font-bold rounded-full hover:bg-brand/20 active:bg-brand/30 active:scale-95 transition-all text-center text-lg"
          >
            Join as Business
          </button>
        </div>

        {/* Feature Highlights (Optional) */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-20 pt-16 border-t border-border-muted/30">
          {/* Feature 1 */}
          <div className="text-center">
            <div className="w-12 h-12 mx-auto mb-4 rounded-lg bg-brand/10 flex items-center justify-center">
              <svg
                className="w-6 h-6 text-brand"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M13 10V3L4 14h7v7l9-11h-7z"
                />
              </svg>
            </div>
            <h3 className="text-white font-bold mb-2">Instantly Verified</h3>
            <p className="text-slate-400 text-sm">
              Real-time verification across databases to spot stolen items
              immediately
            </p>
          </div>

          {/* Feature 2 */}
          <div className="text-center">
            <div className="w-12 h-12 mx-auto mb-4 rounded-lg bg-brand/10 flex items-center justify-center">
              <svg
                className="w-6 h-6 text-brand"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                />
              </svg>
            </div>
            <h3 className="text-white font-bold mb-2">Secure & Encrypted</h3>
            <p className="text-slate-400 text-sm">
              Your data is protected with enterprise-grade encryption and
              security
            </p>
          </div>

          {/* Feature 3 */}
          <div className="text-center">
            <div className="w-12 h-12 mx-auto mb-4 rounded-lg bg-brand/10 flex items-center justify-center">
              <svg
                className="w-6 h-6 text-brand"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"
                />
              </svg>
            </div>
            <h3 className="text-white font-bold mb-2">Community Powered</h3>
            <p className="text-slate-400 text-sm">
              Join users, businesses, and law enforcement in the fight against
              theft
            </p>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-border-muted mt-20">
        <div className="max-w-7xl mx-auto px-6 py-8 text-center text-slate-400 text-sm">
          <p>&copy; 2026 PawnScan. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}
