"use client";

import Link from "next/link";
import { useEffect } from "react";
import { useRouter } from "next/navigation";
import UserDashboardHeader from "@/components/user-dashboard-header";
import { getAuthUser, getJwt } from "@/lib/auth";

export default function CreateReportPage() {
  const router = useRouter();

  useEffect(() => {
    const token = getJwt();
    const authenticatedEmail = getAuthUser();

    if (!token && !authenticatedEmail) {
      router.replace("/login");
    }
  }, [router]);

  return (
    <div className="min-h-screen bg-gradient-to-b from-bg-main to-[#071022] text-slate-200">
      <UserDashboardHeader />

      <main className="mx-auto w-full max-w-4xl px-4 pb-16 pt-36 sm:px-6 sm:pt-40 md:pt-32 lg:px-8">
        <section className="glass-panel rounded-2xl bg-slate-900/35 p-6 shadow-[0_18px_36px_rgba(0,0,0,0.28)] sm:p-8">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand/90">
            Report Item
          </p>
          <h1 className="mt-2 text-2xl font-bold text-white sm:text-3xl">
            Submit a stolen item report
          </h1>
          <p className="mt-3 text-sm text-slate-300">
            Provide complete details so partner businesses and authorities can
            quickly verify and flag suspicious transactions.
          </p>

          <form className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2">
            <label className="flex flex-col gap-2 text-sm">
              <span className="text-slate-300">Item Name</span>
              <input
                type="text"
                placeholder="e.g., Mountain Bike"
                className="rounded-lg bg-slate-900/70 px-3 py-2.5 text-slate-100 placeholder:text-slate-500 shadow-[inset_0_0_0_1px_rgba(71,85,105,0.45)] transition-all duration-200 ease-out focus:outline-none focus:ring-2 focus:ring-brand/50"
              />
            </label>

            <label className="flex flex-col gap-2 text-sm">
              <span className="text-slate-300">Category</span>
              <select className="rounded-lg bg-slate-900/70 px-3 py-2.5 text-slate-100 shadow-[inset_0_0_0_1px_rgba(71,85,105,0.45)] transition-all duration-200 ease-out focus:outline-none focus:ring-2 focus:ring-brand/50">
                <option>Electronics</option>
                <option>Vehicle</option>
                <option>Jewelry</option>
                <option>Documents</option>
                <option>Other</option>
              </select>
            </label>

            <label className="flex flex-col gap-2 text-sm">
              <span className="text-slate-300">Serial Number</span>
              <input
                type="text"
                placeholder="e.g., SN-001122"
                className="rounded-lg bg-slate-900/70 px-3 py-2.5 text-slate-100 placeholder:text-slate-500 shadow-[inset_0_0_0_1px_rgba(71,85,105,0.45)] transition-all duration-200 ease-out focus:outline-none focus:ring-2 focus:ring-brand/50"
              />
            </label>

            <label className="flex flex-col gap-2 text-sm">
              <span className="text-slate-300">Date Lost</span>
              <input
                type="date"
                className="rounded-lg bg-slate-900/70 px-3 py-2.5 text-slate-100 shadow-[inset_0_0_0_1px_rgba(71,85,105,0.45)] transition-all duration-200 ease-out focus:outline-none focus:ring-2 focus:ring-brand/50"
              />
            </label>

            <label className="sm:col-span-2 flex flex-col gap-2 text-sm">
              <span className="text-slate-300">Last Known Location</span>
              <input
                type="text"
                placeholder="e.g., Colon Street, Cebu City"
                className="rounded-lg bg-slate-900/70 px-3 py-2.5 text-slate-100 placeholder:text-slate-500 shadow-[inset_0_0_0_1px_rgba(71,85,105,0.45)] transition-all duration-200 ease-out focus:outline-none focus:ring-2 focus:ring-brand/50"
              />
            </label>

            <label className="sm:col-span-2 flex flex-col gap-2 text-sm">
              <span className="text-slate-300">Description</span>
              <textarea
                rows={5}
                placeholder="Include color, unique marks, accessories, and any details that help identification."
                className="rounded-lg bg-slate-900/70 px-3 py-2.5 text-slate-100 placeholder:text-slate-500 shadow-[inset_0_0_0_1px_rgba(71,85,105,0.45)] transition-all duration-200 ease-out focus:outline-none focus:ring-2 focus:ring-brand/50"
              />
            </label>

            <div className="sm:col-span-2 mt-2 flex flex-wrap gap-3">
              <button
                type="button"
                className="w-full rounded-full bg-brand px-5 py-2.5 text-sm font-semibold text-bg-main transition-all duration-200 ease-out hover:brightness-90 active:scale-[0.98] sm:w-auto"
              >
                Submit Report
              </button>
              <Link
                href="/reports"
                className="w-full rounded-full bg-slate-800/65 px-5 py-2.5 text-center text-sm font-semibold text-slate-200 transition-all duration-200 ease-out hover:bg-slate-700/75 active:scale-[0.98] sm:w-auto"
              >
                Go to My Reports
              </Link>
            </div>
          </form>
        </section>
      </main>
    </div>
  );
}