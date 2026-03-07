"use client";

import { useEffect, useMemo } from "react";
import { useRouter } from "next/navigation";
import { clearAuthSession, getAuthUser, getAuthRole, getJwt } from "@/lib/auth";

export default function DashboardPage() {
  const router = useRouter();

  const userEmail = useMemo(() => {
    if (typeof window === "undefined") {
      return "";
    }

    return getAuthUser() || "";
  }, []);

  const userRole = useMemo(() => {
    if (typeof window === "undefined") {
      return "";
    }

    return getAuthRole() || "";
  }, []);

  useEffect(() => {
    const token = getJwt();
    const authenticatedEmail = getAuthUser();

    if (!token && !authenticatedEmail) {
      router.replace("/login");
    }
  }, [router]);

  function handleLogout() {
    clearAuthSession();
    router.push("/login");
  }

  return (
    <main className="min-h-screen w-full px-4 py-6 sm:px-6 lg:px-8 flex items-center justify-center">
      <section className="mx-auto grid min-h-[calc(100vh-3rem)] w-full max-w-6xl grid-cols-1 overflow-hidden md:grid-cols-1 lg:grid-cols-2">
        <aside className="relative flex flex-col justify-center p-6 sm:p-8 lg:p-12">
          <div
            className="absolute inset-0 bg-gradient-to-br from-brand/15 via-transparent to-transparent"
            aria-hidden="true"
          />
          <div className="relative">
            <p className="mb-2 text-sm font-medium uppercase tracking-[0.12em] text-brand">
              PawnScan
            </p>
            <h1 className="text-3xl font-semibold leading-tight text-slate-100 sm:text-4xl">
              Dashboard
            </h1>
            <p className="mt-4 max-w-xl text-base leading-relaxed text-slate-300">
              You are logged in and can now access PawnScan modules. This is a
              temporary dashboard screen.
            </p>
            <div className="mt-6 inline-flex min-h-12 items-center gap-3 rounded-[10px] border border-border-muted bg-slate-950/70 px-4 py-3 text-slate-200">
              <svg
                aria-hidden="true"
                viewBox="0 0 24 24"
                className="h-6 w-6 text-status-clean"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M12 3l8 4v6c0 5-3.4 8.6-8 10-4.6-1.4-8-5-8-10V7l8-4z" />
                <path d="M9 12l2 2 4-4" />
              </svg>
              <p className="text-sm">Session active and secured.</p>
            </div>
          </div>
        </aside>

        <section className="flex items-center p-4 sm:p-8 lg:p-12">
          <div className="w-full glass-panel p-6 sm:p-8 rounded-md">
            <h2 className="text-2xl font-semibold text-slate-100">Welcome</h2>
            <p className="mt-2 text-sm text-slate-300">
              {userRole === "ADMIN"
                ? "Welcome, System Admin"
                : userEmail
                  ? `Logged in as ${userEmail}`
                  : "You are authenticated."}
            </p>
            <div className="mt-6 rounded-md bg-slate-900/30 p-4 text-sm text-slate-300">
              Temporary dashboard content goes here while the full module pages
              are being built.
            </div>

            <button
              type="button"
              onClick={handleLogout}
              aria-label="Logout"
              className="mt-6 min-h-12 w-full rounded-md bg-slate-800/40 px-4 py-3 text-base font-semibold text-slate-100 transition hover:brightness-95 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/40"
            >
              Logout
            </button>
          </div>
        </section>
      </section>
    </main>
  );
}
