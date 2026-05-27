"use client";

import Link from "next/link";
import React, { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";

type ApiResponse = {
  message?: string;
};

function EyeIcon({ open }: { open: boolean }) {
  if (open) {
    return (
      <svg aria-hidden="true" viewBox="0 0 24 24" className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="2">
        <path d="M3 3l18 18" />
        <path d="M10.58 10.58A2 2 0 0012 14a2 2 0 001.42-.58" />
        <path d="M9.88 5.09A9.77 9.77 0 0112 5c5.2 0 9.27 3.11 10.58 7a10.74 10.74 0 01-4.27 5.36" />
        <path d="M6.61 6.61A10.75 10.75 0 001.42 12a10.74 10.74 0 003.26 4.62" />
      </svg>
    );
  }
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M1.42 12C2.73 8.11 6.8 5 12 5s9.27 3.11 10.58 7c-1.31 3.89-5.38 7-10.58 7S2.73 15.89 1.42 12z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

function PasswordStrengthBar({ password }: { password: string }) {
  const getStrength = () => {
    if (!password) return 0;
    let score = 0;
    if (password.length >= 8) score++;
    if (password.length >= 12) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;
    return score;
  };

  const strength = getStrength();
  const labels = ["", "Very Weak", "Weak", "Fair", "Strong", "Very Strong"];
  const colors = ["", "bg-red-500", "bg-orange-400", "bg-yellow-400", "bg-brand", "bg-brand"];

  if (!password) return null;

  return (
    <div className="mt-2 space-y-1">
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map((i) => (
          <div
            key={i}
            className={`h-1 flex-1 rounded-full transition-all duration-300 ${
              i <= strength ? colors[strength] : "bg-slate-700"
            }`}
          />
        ))}
      </div>
      <p className={`text-xs ${strength <= 2 ? "text-orange-400" : strength >= 4 ? "text-brand" : "text-yellow-400"}`}>
        {labels[strength]}
      </p>
    </div>
  );
}

export default function ResetPasswordForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [token, setToken] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitAttempted, setSubmitAttempted] = useState(false);
  const [success, setSuccess] = useState(false);
  const [apiMessage, setApiMessage] = useState<{
    type: "error" | "success";
    text: string;
  } | null>(null);

  useEffect(() => {
    const t = searchParams.get("token");
    if (t) setToken(t);
  }, [searchParams]);

  const passwordError =
    newPassword.length > 0 && newPassword.length < 8
      ? "Password must be at least 8 characters"
      : null;

  const confirmError =
    confirmPassword.length > 0 && confirmPassword !== newPassword
      ? "Passwords do not match"
      : null;

  const tokenMissing = !token.trim();
  const canSubmit =
    !tokenMissing &&
    newPassword.length >= 8 &&
    !confirmError &&
    !isSubmitting;

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitAttempted(true);
    setApiMessage(null);

    if (!canSubmit) return;

    setIsSubmitting(true);

    try {
      const response = await fetch("/api/auth/reset-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token, newPassword }),
      });

      const data = (await response.json().catch(() => ({}))) as ApiResponse;

      if (!response.ok) {
        setApiMessage({
          type: "error",
          text: data.message || "Password reset failed. The link may have expired.",
        });
        return;
      }

      setSuccess(true);
      setTimeout(() => router.push("/login"), 3000);
    } catch {
      setApiMessage({
        type: "error",
        text: "We could not reach the server. Please try again.",
      });
    } finally {
      setIsSubmitting(false);
    }
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
              {success ? "Password Updated" : "Create New Password"}
            </h1>
            <p className="mt-6 text-lg leading-relaxed text-slate-400 font-light max-w-md">
              {success
                ? "Your password has been successfully reset. Redirecting to login..."
                : "Choose a strong password to keep your PawnScan account secure."}
            </p>
          </div>
        </aside>

        <section className="flex items-center p-4 sm:p-8 lg:p-12 relative z-10">
          <div className="w-full max-w-[28rem] mx-auto p-8 sm:p-10 rounded-3xl shadow-[0_24px_60px_rgba(0,0,0,0.4)] bg-slate-900/60 border border-slate-700/40 backdrop-blur-2xl relative overflow-hidden transition-all duration-300 hover:shadow-[0_24px_80px_rgba(0,0,0,0.5)]">
            <div className="absolute top-0 left-1/2 -translate-x-1/2 w-3/4 h-[2px] bg-gradient-to-r from-transparent via-brand/80 to-transparent" />

            {success ? (
              <div className="flex flex-col items-center text-center py-4">
                <div className="flex h-16 w-16 items-center justify-center rounded-full bg-brand/10 border border-brand/30 mb-4">
                  <svg viewBox="0 0 24 24" className="h-8 w-8 text-brand" fill="none" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <h2 className="text-2xl font-semibold text-slate-100">Success!</h2>
                <p className="mt-3 text-sm text-slate-300 leading-relaxed">
                  Your password has been reset. Redirecting you to login in a moment...
                </p>
                <Link
                  href="/login"
                  className="mt-6 min-h-12 w-full rounded-md bg-brand px-4 py-3 text-base font-semibold text-slate-950 transition hover:brightness-90 active:scale-95 text-center block"
                >
                  Go to Login
                </Link>
              </div>
            ) : (
              <>
                {tokenMissing && (
                  <div className="mb-4 rounded-[10px] border border-status-stolen/50 bg-status-stolen/10 px-3 py-2 text-sm text-status-stolen">
                    Invalid or missing reset link. Please request a new one.
                  </div>
                )}

                <h2 className="text-2xl font-semibold text-slate-100">Set New Password</h2>
                <p className="mt-2 text-sm text-slate-300">
                  Must be at least 8 characters
                </p>

                <form
                  className="mt-6 space-y-4"
                  onSubmit={handleSubmit}
                  noValidate
                  aria-label="Reset password form"
                >
                  {/* New Password */}
                  <div>
                    <label
                      htmlFor="new-password"
                      className="mb-1 block text-sm font-medium text-slate-200"
                    >
                      New Password
                    </label>
                    <div className="flex gap-2">
                      <input
                        id="new-password"
                        aria-label="New Password"
                        type={showPassword ? "text" : "password"}
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                        autoComplete="new-password"
                      />
                      <button
                        type="button"
                        aria-label={showPassword ? "Hide password" : "Show password"}
                        onClick={() => setShowPassword((v) => !v)}
                        className="inline-flex min-h-12 min-w-12 items-center justify-center rounded-[10px] border border-border-muted bg-slate-900 px-3 text-slate-200 transition hover:border-brand hover:bg-slate-800 active:scale-95 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                      >
                        <EyeIcon open={showPassword} />
                      </button>
                    </div>
                    <PasswordStrengthBar password={newPassword} />
                    {submitAttempted && passwordError && (
                      <p className="mt-1 text-sm text-status-stolen">{passwordError}</p>
                    )}
                  </div>

                  {/* Confirm Password */}
                  <div>
                    <label
                      htmlFor="confirm-password"
                      className="mb-1 block text-sm font-medium text-slate-200"
                    >
                      Confirm Password
                    </label>
                    <div className="flex gap-2">
                      <input
                        id="confirm-password"
                        aria-label="Confirm Password"
                        type={showConfirm ? "text" : "password"}
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                        autoComplete="new-password"
                      />
                      <button
                        type="button"
                        aria-label={showConfirm ? "Hide confirm password" : "Show confirm password"}
                        onClick={() => setShowConfirm((v) => !v)}
                        className="inline-flex min-h-12 min-w-12 items-center justify-center rounded-[10px] border border-border-muted bg-slate-900 px-3 text-slate-200 transition hover:border-brand hover:bg-slate-800 active:scale-95 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                      >
                        <EyeIcon open={showConfirm} />
                      </button>
                    </div>
                    {(submitAttempted || confirmPassword.length > 0) && confirmError && (
                      <p className="mt-1 text-sm text-status-stolen">{confirmError}</p>
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
                    aria-label="Reset password"
                    disabled={!canSubmit}
                    className="min-h-12 w-full rounded-md bg-brand px-4 py-3 text-base font-semibold text-slate-950 transition hover:brightness-90 active:scale-95 active:brightness-85 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/40 disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    {isSubmitting ? "Resetting..." : "Reset Password"}
                  </button>
                </form>

                <p className="mt-6 text-sm text-slate-300 text-center">
                  <Link
                    href="/forgot-password"
                    className="font-medium text-brand transition hover:underline"
                  >
                    Request a new reset link
                  </Link>
                </p>
              </>
            )}
          </div>
        </section>
      </section>
    </main>
  );
}
