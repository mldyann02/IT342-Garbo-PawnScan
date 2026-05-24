"use client";

import { useRouter } from "next/navigation";
import Image from "next/image";

export default function LandingPage() {
  const router = useRouter();

  const handleReportClick = () => {
    router.push("/register?role=USER");
  };

  const handleBusinessClick = () => {
    router.push("/register?role=BUSINESS");
  };

  return (
    <div className="relative min-h-screen text-slate-200 overflow-hidden">
      {/* Background Decorative Elements */}
      <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] rounded-full bg-brand/10 blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[50%] h-[50%] rounded-full bg-[#071022]/80 blur-[150px] pointer-events-none" />
      <div className="absolute top-[40%] right-[10%] w-[30%] h-[30%] rounded-full bg-brand/5 blur-[100px] pointer-events-none" />
      
      {/* Subtle Background Icons */}
      <div className="absolute top-[10%] left-[2%] w-[20rem] h-[20rem] opacity-[0.03] pointer-events-none -rotate-12 mix-blend-screen">
        <Image src="/lock.png" alt="Lock" fill className="object-contain" />
      </div>
      <div className="absolute top-[30%] right-[2%] w-[25rem] h-[25rem] opacity-[0.03] pointer-events-none rotate-12 mix-blend-screen">
        <Image src="/secure.png" alt="Secure" fill className="object-contain" />
      </div>
      
      {/* Main Content */}
      <main className="relative z-10 max-w-5xl mx-auto px-6 pb-16 pt-40 md:pb-32 md:pt-48 flex flex-col items-center">
        {/* Headline */}
        <div className="text-center mb-8 w-full max-w-3xl">
          <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight leading-[1.1] mb-6">
            <span className="text-white drop-shadow-sm">Protect your assets,</span>
            <br />
            <span className="text-white drop-shadow-sm">secure your</span>
            <br />
            <span className="text-brand drop-shadow-[0_0_20px_rgba(0,211,127,0.3)]">business.</span>
          </h1>
        </div>

        {/* Subheadline */}
        <p className="text-center text-slate-400 text-lg md:text-2xl font-light max-w-2xl mx-auto mb-14 leading-relaxed">
          Connect individuals and businesses to prevent stolen items from entering the market. Report, verify, and secure transactions with confidence.
        </p>

        {/* CTA Buttons */}
        <div className="flex flex-col sm:flex-row gap-5 justify-center w-full max-w-lg mx-auto">
          <button
            onClick={handleReportClick}
            className="flex-1 px-8 py-4 bg-brand text-bg-main font-bold rounded-full transition-all duration-300 shadow-[0_0_30px_rgba(0,211,127,0.2)] hover:shadow-[0_0_40px_rgba(0,211,127,0.4)] hover:brightness-95 text-lg"
          >
            <span>Report an Item</span>
          </button>
          <button
            onClick={handleBusinessClick}
            className="flex-1 px-8 py-4 bg-transparent border border-brand/50 text-white font-semibold rounded-full hover:bg-brand/10 hover:border-brand active:scale-95 transition-all duration-300 text-lg backdrop-blur-sm"
          >
            Partner as Business
          </button>
        </div>

        {/* Feature Highlights */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-32 w-full max-w-5xl">
          {/* Feature 1 */}
          <div className="group p-8 rounded-3xl bg-slate-900/40 border border-slate-800/50 backdrop-blur-md hover:bg-slate-900/60 hover:border-brand/30 transition-all duration-500 hover:-translate-y-2 hover:shadow-[0_10px_40px_-10px_rgba(0,211,127,0.15)]">
            <div className="w-14 h-14 mb-6 rounded-2xl bg-brand/10 border border-brand/20 flex items-center justify-center group-hover:scale-110 group-hover:bg-brand/20 transition-all duration-300">
              <svg
                className="w-7 h-7 text-brand"
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
            <h3 className="text-white text-xl font-bold mb-3 tracking-wide">Instant Verification</h3>
            <p className="text-slate-400 leading-relaxed font-light">
              Seamlessly cross-reference items in real-time to identify potential risks before finalizing transactions.
            </p>
          </div>

          {/* Feature 2 */}
          <div className="group p-8 rounded-3xl bg-slate-900/40 border border-slate-800/50 backdrop-blur-md hover:bg-slate-900/60 hover:border-brand/30 transition-all duration-500 hover:-translate-y-2 hover:shadow-[0_10px_40px_-10px_rgba(0,211,127,0.15)]">
            <div className="w-14 h-14 mb-6 rounded-2xl bg-brand/10 border border-brand/20 flex items-center justify-center group-hover:scale-110 group-hover:bg-brand/20 transition-all duration-300">
              <svg
                className="w-7 h-7 text-brand"
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
            <h3 className="text-white text-xl font-bold mb-3 tracking-wide">Enterprise Security</h3>
            <p className="text-slate-400 leading-relaxed font-light">
              Your data remains strictly confidential, protected by industry-leading encryption protocols.
            </p>
          </div>

          {/* Feature 3 */}
          <div className="group p-8 rounded-3xl bg-slate-900/40 border border-slate-800/50 backdrop-blur-md hover:bg-slate-900/60 hover:border-brand/30 transition-all duration-500 hover:-translate-y-2 hover:shadow-[0_10px_40px_-10px_rgba(0,211,127,0.15)]">
            <div className="w-14 h-14 mb-6 rounded-2xl bg-brand/10 border border-brand/20 flex items-center justify-center group-hover:scale-110 group-hover:bg-brand/20 transition-all duration-300">
              <svg
                className="w-7 h-7 text-brand"
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
            <h3 className="text-white text-xl font-bold mb-3 tracking-wide">Community Integrity</h3>
            <p className="text-slate-400 leading-relaxed font-light">
              Collaborate with individuals and trusted partners to maintain a clean and reliable marketplace.
            </p>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="relative z-10 border-t border-slate-800/60 mt-10 bg-slate-900/20 backdrop-blur-sm">
        <div className="max-w-7xl mx-auto px-6 py-10 flex flex-col md:flex-row items-center justify-between text-slate-500 text-sm">
          <p>&copy; {new Date().getFullYear()} PawnScan. All rights reserved.</p>
          <div className="flex gap-6 mt-4 md:mt-0">
            <button className="hover:text-brand transition-colors">Privacy Policy</button>
            <button className="hover:text-brand transition-colors">Terms of Service</button>
          </div>
        </div>
      </footer>
    </div>
  );
}


