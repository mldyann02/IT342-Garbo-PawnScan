"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";

export default function Header() {
  const router = useRouter();

  return (
    <nav className="fixed left-1/2 top-4 z-50 w-[92%] max-w-5xl -translate-x-1/2 rounded-full bg-gradient-to-r from-slate-900/55 via-slate-900/45 to-slate-800/50 backdrop-blur-xl shadow-[0_18px_38px_rgba(0,0,0,0.35)] sm:top-6">
      <div className="flex items-center justify-between px-6 py-3 sm:px-8">
        {/* Brand - Clickable to redirect to landing */}
        <button
          onClick={() => router.push("/landing")}
          className="text-brand font-bold text-xl tracking-wide hover:text-opacity-80 transition-all duration-300 cursor-pointer"
        >
          PawnScan
        </button>

        {/* Right side - Sign In & Get Started */}
        <div className="flex items-center gap-6">
          <Link
            href="/login"
            className="text-white text-sm font-medium hover:text-brand transition-colors"
          >
            Sign In
          </Link>
          <button
            onClick={() => router.push("/register")}
            className="px-6 py-2.5 bg-brand text-bg-main font-semibold rounded-full text-sm hover:brightness-90 hover:shadow-lg hover:shadow-brand/50 active:scale-95 active:brightness-85 transition-all duration-300"
          >
            Get Started
          </button>
        </div>
      </div>
    </nav>
  );
}
