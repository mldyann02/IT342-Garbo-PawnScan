import { Metadata } from "next";
import { VerifyOtpForm } from "@/features/auth/components/verify-otp-form";
import Link from "next/link";

export const metadata: Metadata = {
  title: "Verify Email - PawnScan",
  description: "Verify your email to continue",
};

export default function VerifyOtpPage() {
  return (
    <div className="container relative flex h-screen flex-col items-center justify-center md:grid lg:max-w-none lg:grid-cols-2 lg:px-0">
      <div className="relative hidden h-full flex-col bg-slate-950 p-10 text-white lg:flex overflow-hidden">
        <div className="absolute inset-0 bg-brand/5" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,_var(--tw-gradient-stops))] from-brand/20 via-slate-950/0 to-slate-950/0" />
        
        <div className="relative z-20 flex items-center text-lg font-medium">
          <Link href="/" className="flex items-center gap-2 transition-opacity hover:opacity-80">
            <div className="bg-brand/20 p-2 rounded-lg">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="text-brand"
              >
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10" />
              </svg>
            </div>
            <span className="text-xl font-bold tracking-tight">PawnScan</span>
          </Link>
        </div>
        
        <div className="relative z-20 mt-auto">
          <blockquote className="space-y-2">
            <p className="text-lg text-slate-300">
              "We take security seriously. Email verification ensures that all our users are legitimate, protecting both individuals and businesses."
            </p>
          </blockquote>
        </div>
      </div>
      <div className="lg:p-8 w-full max-w-sm mx-auto p-4">
        <VerifyOtpForm />
      </div>
    </div>
  );
}
