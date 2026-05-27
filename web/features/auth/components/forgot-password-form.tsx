"use client";

import Link from "next/link";
import React, { useState } from "react";

type ApiResponse = {
  message?: string;
};

export default function ForgotPasswordForm() {
  const [email, setEmail] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [apiMessage, setApiMessage] = useState<{
    type: "error" | "success";
    text: string;
  } | null>(null);

  const emailError =
    email.length > 0 && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
      ? "Please enter a valid email address"
      : null;

  const canSubmit =
    email.trim().length > 0 && !emailError && !isSubmitting;

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setApiMessage(null);

    if (emailError || !email.trim()) return;

    setIsSubmitting(true);

    try {
      const response = await fetch("/api/auth/forgot-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email.trim() }),
      });

      const data = (await response.json().catch(() => ({}))) as ApiResponse;

      if (!response.ok) {
        setApiMessage({
          type: "error",
          text: data.message || "Something went wrong. Please try again.",
        });
        return;
      }

      // Always show success to avoid enumeration
      setSubmitted(true);
    } catch {
      setApiMessage({
        type: "error",
        text: "We could not reach the server. Please try again.",
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  if (submitted) {
    return (
      <main className="min-h-screen w-full px-4 py-6 sm:px-6 lg:px-8 flex items-center justify-center">
        <section className="mx-auto grid min-h-[calc(100vh-3rem)] w-full max-w-6xl grid-cols-1 overflow-hidden lg:grid-cols-2">
          <aside className="relative flex flex-col justify-center p-6 sm:p-8 lg:p-12 lg:sticky lg:top-24 lg:self-start">
            <div
              className="absolute inset-0 bg-gradient-to-br from-brand/15 via-transparent to-transparent"
              aria-hidden="true"
            />
            <div className="relative z-10 max-w-lg">
              <div className="inline-flex items-center gap-2 rounded-full border border-brand/30 bg-brand/10 px-3 py-1.5 mb-6 text-sm font-medium text-brand/90 backdrop-blur-sm">
                <span className="h-1.5 w-1.5 rounded-full bg-brand animate-pulse" />
                Secure Reset
              </div>
              <h1 className="text-4xl font-extrabold tracking-tight sm:text-5xl lg:text-6xl text-transparent bg-clip-text bg-gradient-to-br from-white via-slate-200 to-slate-500 pb-2">
                Check Your Email
              </h1>
              <p className="mt-6 text-lg leading-relaxed text-slate-400 font-light max-w-md">
                We&apos;ve sent a reset link if that email is registered with PawnScan.
              </p>
            </div>
          </aside>

          <section className="flex items-center p-4 sm:p-8 lg:p-12 relative z-10">
            <div className="w-full max-w-[28rem] mx-auto p-8 sm:p-10 rounded-3xl shadow-[0_24px_60px_rgba(0,0,0,0.4)] bg-slate-900/60 border border-slate-700/40 backdrop-blur-2xl relative overflow-hidden">
              <div className="absolute top-0 left-1/2 -translate-x-1/2 w-3/4 h-[2px] bg-gradient-to-r from-transparent via-brand/80 to-transparent" />

              {/* Success icon */}
              <div className="flex justify-center mb-6">
                <div className="flex h-16 w-16 items-center justify-center rounded-full bg-brand/10 border border-brand/30">
                  <svg viewBox="0 0 24 24" className="h-8 w-8 text-brand" fill="none" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                  </svg>
                </div>
              </div>

              <h2 className="text-2xl font-semibold text-slate-100 text-center">Reset Link Sent</h2>
              <p className="mt-3 text-sm text-slate-300 text-center leading-relaxed">
                If <span className="text-brand font-medium">{email}</span> is registered with
                PawnScan, you&apos;ll receive a password reset link shortly.
              </p>
              <p className="mt-3 text-xs text-slate-400 text-center">
                The link expires in <strong className="text-slate-300">60 minutes</strong>. Check your
                spam folder if you don&apos;t see it.
              </p>

              <div className="mt-8 space-y-3">
                <Link
                  href="/login"
                  className="block min-h-12 w-full rounded-md bg-brand px-4 py-3 text-base font-semibold text-slate-950 transition hover:brightness-90 active:scale-95 text-center"
                >
                  Back to Login
                </Link>
                <button
                  type="button"
                  onClick={() => { setSubmitted(false); setEmail(""); }}
                  className="block min-h-12 w-full rounded-md border border-slate-700 px-4 py-3 text-sm font-medium text-slate-300 transition hover:border-brand/50 hover:text-white text-center"
                >
                  Try a different email
                </button>
              </div>
            </div>
          </section>
        </section>
      </main>
    );
  }

  return (
    <main className="min-h-screen w-full px-4 py-6 sm:px-6 lg:px-8 flex items-center justify-center">
      <section className="mx-auto grid min-h-[calc(100vh-3rem)] w-full max-w-6xl grid-cols-1 overflow-hidden lg:grid-cols-2">
        <aside className="relative flex flex-col justify-center p-6 sm:p-8 lg:p-12 lg:sticky lg:top-24 lg:self-start">
          <div
            className="absolute inset-0 bg-gradient-to-br from-brand/15 via-transparent to-transparent"
            aria-hidden="true"
          />
          <div className="relative z-10 max-w-lg">
            <div className="inline-flex items-center gap-2 rounded-full border border-brand/30 bg-brand/10 px-3 py-1.5 mb-6 text-sm font-medium text-brand/90 backdrop-blur-sm">
              <span className="h-1.5 w-1.5 rounded-full bg-brand animate-pulse" />
              Account Recovery
            </div>
            <h1 className="text-4xl font-extrabold tracking-tight sm:text-5xl lg:text-6xl text-transparent bg-clip-text bg-gradient-to-br from-white via-slate-200 to-slate-500 pb-2">
              Reset Your Password
            </h1>
            <p className="mt-6 text-lg leading-relaxed text-slate-400 font-light max-w-md">
              Enter your registered email address and we&apos;ll send you a secure link
              to reset your password.
            </p>
          </div>
        </aside>

        <section className="flex items-center p-4 sm:p-8 lg:p-12 relative z-10">
          <div className="w-full max-w-[28rem] mx-auto p-8 sm:p-10 rounded-3xl shadow-[0_24px_60px_rgba(0,0,0,0.4)] bg-slate-900/60 border border-slate-700/40 backdrop-blur-2xl relative overflow-hidden transition-all duration-300 hover:shadow-[0_24px_80px_rgba(0,0,0,0.5)]">
            <div className="absolute top-0 left-1/2 -translate-x-1/2 w-3/4 h-[2px] bg-gradient-to-r from-transparent via-brand/80 to-transparent" />
            <h2 className="text-2xl font-semibold text-slate-100">Forgot Password</h2>
            <p className="mt-2 text-sm text-slate-300">
              We&apos;ll send a reset link to your email address
            </p>

            <form
              className="mt-6 space-y-4"
              onSubmit={handleSubmit}
              noValidate
              aria-label="Forgot password form"
            >
              <div>
                <label
                  htmlFor="forgot-email"
                  className="mb-1 block text-sm font-medium text-slate-200"
                >
                  Email Address
                </label>
                <input
                  id="forgot-email"
                  aria-label="Email Address"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                  autoComplete="email"
                  placeholder="you@example.com"
                />
                {emailError && (
                  <p className="mt-1 text-sm text-status-stolen">{emailError}</p>
                )}
              </div>

              {apiMessage && (
                <div
                  role="status"
                  aria-live="polite"
                  className={`rounded-[10px] border px-3 py-2 text-sm ${
                    apiMessage.type === "error"
                      ? "border-status-stolen/50 bg-status-stolen/10 text-status-stolen"
                      : "border-status-clean/40 bg-status-clean/10 text-status-clean"
                  }`}
                >
                  {apiMessage.text}
                </div>
              )}

              <button
                type="submit"
                aria-label="Send reset link"
                disabled={!canSubmit}
                className="min-h-12 w-full rounded-md bg-brand px-4 py-3 text-base font-semibold text-slate-950 transition hover:brightness-90 active:scale-95 active:brightness-85 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/40 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {isSubmitting ? "Sending..." : "Send Reset Link"}
              </button>
            </form>

            <p className="mt-6 text-sm text-slate-300 text-center">
              Remember your password?{" "}
              <Link
                href="/login"
                className="font-medium text-brand transition hover:underline"
              >
                Back to login.
              </Link>
            </p>
          </div>
        </section>
      </section>
    </main>
  );
}
