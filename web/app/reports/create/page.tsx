"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import UserDashboardHeader from "@/components/user-dashboard-header";
import { getAuthUser, getJwt } from "@/lib/auth";
import { createReport } from "@/lib/reports";

type ReportForm = {
  serialNumber: string;
  itemModel: string;
  description: string;
  file: File | null;
};

export default function CreateReportPage() {
  const router = useRouter();
  const [form, setForm] = useState<ReportForm>({
    serialNumber: "",
    itemModel: "",
    description: "",
    file: null,
  });
  const [submitAttempted, setSubmitAttempted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [feedback, setFeedback] = useState<{
    type: "success" | "error";
    message: string;
  } | null>(null);

  useEffect(() => {
    const token = getJwt();
    const authenticatedEmail = getAuthUser();

    if (!token && !authenticatedEmail) {
      router.replace("/login");
    }
  }, [router]);

  const errors = useMemo(() => {
    const next: Partial<Record<keyof Omit<ReportForm, "file">, string>> = {};

    if (!form.serialNumber.trim()) {
      next.serialNumber = "Serial number is required";
    }

    if (!form.itemModel.trim()) {
      next.itemModel = "Item model is required";
    }

    if (!form.description.trim()) {
      next.description = "Description is required";
    }

    return next;
  }, [form]);

  const canSubmit = Object.keys(errors).length === 0 && !isSubmitting;

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitAttempted(true);
    setFeedback(null);

    if (Object.keys(errors).length > 0) {
      return;
    }

    setIsSubmitting(true);

    try {
      await createReport({
        serialNumber: form.serialNumber,
        itemModel: form.itemModel,
        description: form.description,
        file: form.file,
      });

      setFeedback({
        type: "success",
        message: "Report created successfully. Redirecting to My Reports...",
      });

      setTimeout(() => {
        router.push("/reports?created=1");
      }, 500);
    } catch (error) {
      setFeedback({
        type: "error",
        message: error instanceof Error ? error.message : "Failed to create report",
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-bg-main to-[#071022] text-slate-200">
      <UserDashboardHeader />

      <main className="mx-auto w-full max-w-4xl px-4 pb-16 pt-36 sm:px-6 sm:pt-40 md:pt-32 lg:px-8">
        <section className="glass-panel rounded-2xl bg-slate-900/35 p-6 shadow-[0_18px_36px_rgba(0,0,0,0.28)] sm:p-8">
          <h1 className="text-2xl font-bold text-white sm:text-3xl">
            Submit a stolen item report
          </h1>
          <p className="mt-3 text-sm text-slate-300">
            Provide key identifiers so pawnshops and partner businesses can detect potentially stolen items.
          </p>

          <form className="mt-6 grid grid-cols-1 gap-4" onSubmit={handleSubmit} noValidate>
            <label className="flex flex-col gap-2 text-sm">
              <span className="text-slate-300">Serial Number</span>
              <input
                type="text"
                value={form.serialNumber}
                onChange={(event) =>
                  setForm((current) => ({ ...current, serialNumber: event.target.value }))
                }
                placeholder="e.g., SN-001122"
                className="rounded-lg bg-slate-900/70 px-3 py-2.5 text-slate-100 placeholder:text-slate-500 shadow-[inset_0_0_0_1px_rgba(71,85,105,0.45)] transition-all duration-200 ease-out focus:outline-none focus:ring-2 focus:ring-brand/50"
              />
              {submitAttempted && errors.serialNumber && (
                <p className="text-sm text-status-stolen">{errors.serialNumber}</p>
              )}
            </label>

            <label className="flex flex-col gap-2 text-sm">
              <span className="text-slate-300">Item Model</span>
              <input
                type="text"
                value={form.itemModel}
                onChange={(event) =>
                  setForm((current) => ({ ...current, itemModel: event.target.value }))
                }
                placeholder="e.g., MacBook Pro 14-inch"
                className="rounded-lg bg-slate-900/70 px-3 py-2.5 text-slate-100 placeholder:text-slate-500 shadow-[inset_0_0_0_1px_rgba(71,85,105,0.45)] transition-all duration-200 ease-out focus:outline-none focus:ring-2 focus:ring-brand/50"
              />
              {submitAttempted && errors.itemModel && (
                <p className="text-sm text-status-stolen">{errors.itemModel}</p>
              )}
            </label>

            <label className="flex flex-col gap-2 text-sm">
              <span className="text-slate-300">Description</span>
              <textarea
                rows={5}
                value={form.description}
                onChange={(event) =>
                  setForm((current) => ({ ...current, description: event.target.value }))
                }
                placeholder="Include identifying marks, condition, accessories, and context."
                className="rounded-lg bg-slate-900/70 px-3 py-2.5 text-slate-100 placeholder:text-slate-500 shadow-[inset_0_0_0_1px_rgba(71,85,105,0.45)] transition-all duration-200 ease-out focus:outline-none focus:ring-2 focus:ring-brand/50"
              />
              {submitAttempted && errors.description && (
                <p className="text-sm text-status-stolen">{errors.description}</p>
              )}
            </label>

            <label className="flex flex-col gap-2 text-sm">
              <span className="text-slate-300">Upload Evidence (optional image or PDF)</span>
              <input
                type="file"
                accept="image/*,.pdf"
                onChange={(event) =>
                  setForm((current) => ({ ...current, file: event.target.files?.[0] ?? null }))
                }
                className="rounded-lg bg-slate-900/70 px-3 py-2.5 text-slate-200 file:mr-3 file:rounded-full file:border-0 file:bg-brand file:px-4 file:py-2 file:text-sm file:font-semibold file:text-bg-main hover:file:brightness-90"
              />
            </label>

            {feedback && (
              <div
                className={`rounded-lg border px-4 py-3 text-sm ${
                  feedback.type === "error"
                    ? "border-status-stolen/40 bg-status-stolen/10 text-status-stolen"
                    : "border-status-clean/40 bg-status-clean/10 text-status-clean"
                }`}
              >
                {feedback.message}
              </div>
            )}

            <div className="mt-2 flex flex-wrap justify-end gap-3">
              <button
                type="submit"
                disabled={!canSubmit}
                className="w-full rounded-full bg-brand px-5 py-2.5 text-sm font-semibold text-bg-main transition-all duration-200 ease-out hover:brightness-90 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-60 sm:w-auto"
              >
                {isSubmitting ? "Submitting..." : "Submit Report"}
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
