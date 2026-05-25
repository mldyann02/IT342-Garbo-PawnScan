"use client";

import { useState } from "react";
import { storeAuthUser, storeJwt, storeAuthRole } from "@/shared/auth";
import { useRouter, useSearchParams } from "next/navigation";

export function VerifyOtpForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const emailParam = searchParams.get("email") || "";

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [code, setCode] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!emailParam) {
      setError("Email is missing. Please try registering again.");
      return;
    }
    if (code.length < 6) {
      setError("Please enter a valid 6-digit code.");
      return;
    }

    setIsLoading(true);
    setError("");

    try {
      const res = await fetch("/api/auth/verify-otp", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email: emailParam, code }),
      });

      const data = await res.json();

      if (!res.ok) {
        throw new Error(data.error || "Failed to verify OTP");
      }

      if (data.token || data.jwt || data.accessToken || data.access_token) {
        const token = data.token || data.jwt || data.accessToken || data.access_token;
        storeJwt(token);
        
        const role = data.role || data.user?.role || "";
        if (role) {
          storeAuthRole(role);
        }
        
        storeAuthUser(emailParam);

        if (data.registrationStatus === "INCOMPLETE") {
          router.push("/complete-profile");
        } else if (role === "ADMIN") {
          router.push("/admin/dashboard");
        } else if (role === "BUSINESS") {
          router.push("/business");
        } else {
          router.push("/dashboard");
        }
      } else {
        router.push("/login?verified=true");
      }
    } catch (err: any) {
      setError(err.message || "An unexpected error occurred");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="w-full">
      <div className="flex flex-col space-y-2 text-center mb-8">
        <div className="mx-auto bg-brand/10 p-3 rounded-full w-16 h-16 flex items-center justify-center mb-2">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="32"
            height="32"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            className="text-brand"
          >
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10" />
            <path d="m9 12 2 2 4-4" />
          </svg>
        </div>
        <h1 className="text-2xl font-bold tracking-tight text-white">
          Verify your email
        </h1>
        <p className="text-sm text-slate-400">
          We sent a 6-digit verification code to <span className="text-white font-medium">{emailParam}</span>
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="space-y-2">
          <label htmlFor="code" className="text-sm font-medium text-slate-200">
            Verification Code
          </label>
          <input
            id="code"
            type="text"
            inputMode="numeric"
            pattern="[0-9]*"
            maxLength={6}
            value={code}
            onChange={(e) => setCode(e.target.value)}
            disabled={isLoading}
            className="w-full rounded-[10px] border border-slate-700/50 bg-slate-900/50 px-3 py-2 text-center text-2xl tracking-[0.5em] h-14 text-slate-100 placeholder:text-slate-500 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
            placeholder="000000"
            required
          />
        </div>

        {error && (
          <div className="rounded-[10px] border border-red-500/50 bg-red-500/10 px-3 py-2 text-sm text-red-500 flex items-center gap-2">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <circle cx="12" cy="12" r="10" />
              <line x1="12" x2="12" y1="8" y2="12" />
              <line x1="12" x2="12.01" y1="16" y2="16" />
            </svg>
            {error}
          </div>
        )}

        <button
          type="submit"
          className="w-full min-h-12 rounded-md bg-brand px-4 py-3 text-base font-semibold text-slate-950 transition hover:brightness-90 active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
          disabled={isLoading || code.length < 6}
        >
          {isLoading ? "Verifying..." : "Verify Account"}
        </button>
      </form>
    </div>
  );
}
