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
    const next: Partial<Record<keyof ReportForm, string>> = {};

    if (!form.serialNumber.trim()) {
      next.serialNumber = "Serial number is required";
    }

    if (!form.itemModel.trim()) {
      next.itemModel = "Item model is required";
    }

    if (!form.description.trim()) {
      next.description = "Description is required";
    }

    if (!form.file) {
      next.file = "Image or PDF evidence is required";
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
        message:
          error instanceof Error ? error.message : "Failed to create report",
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-bg-main via-slate-900/50 to-[#071022] text-slate-200 relative overflow-hidden">
      {/* Decorative background elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 -left-48 w-96 h-96 bg-brand/15 rounded-full blur-3xl" />
        <div className="absolute bottom-1/4 -right-48 w-96 h-96 bg-brand/10 rounded-full blur-3xl" />
        <div className="absolute top-3/4 left-1/2 w-80 h-80 bg-brand/5 rounded-full blur-3xl" />
      </div>

      <UserDashboardHeader />

      <main className="relative z-10 mx-auto w-full max-w-6xl px-4 pb-16 pt-32 sm:px-6 lg:px-8">
        <section className="bg-[#0a1628]/80 backdrop-blur-xl border border-white/5 rounded-3xl p-6 sm:p-10 shadow-2xl">
          <div className="w-full mx-auto">
            {/* Header Section */}
            <div className="mb-8">
              <div className="flex items-end gap-5">
                <div>
                  <h1 className="text-4xl sm:text-3xl font-bold text-white tracking-tight leading-tight">
                    Report a Stolen Item
                  </h1>
                  <p className="mt-1 text-base text-slate-400 font-medium">
                    Help recover your property and protect the community
                  </p>
                </div>
              </div>
            </div>

            {/* Info Banner */}
            <div className="bg-gradient-to-r from-brand/15 to-brand/5 border border-brand/30 rounded-xl p-4 mb-10">
              <div className="flex gap-3">
                <div className="flex-shrink-0 w-5 h-5 mt-0.5 text-brand">
                  <svg fill="currentColor" viewBox="0 0 20 20">
                    <path
                      fillRule="evenodd"
                      d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                      clipRule="evenodd"
                    />
                  </svg>
                </div>
                <p className="text-sm text-slate-300">
                  Provide detailed information about your stolen item. This
                  helps pawnshops and partner businesses identify and recover
                  your property.
                </p>
              </div>
            </div>

            <form className="space-y-8" onSubmit={handleSubmit} noValidate>
              {/* Item Information Section */}
              <div className="bg-slate-800/40 backdrop-blur-sm border border-slate-700/50 rounded-2xl p-6 sm:p-8">
                <h2 className="text-2xl font-bold text-white mb-7 flex items-center gap-3">
                  <svg
                    className="w-6 h-6 text-brand flex-shrink-0"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"
                    />
                  </svg>
                  Item Details
                </h2>
                <div className="space-y-6">
                  <label className="flex flex-col gap-3 text-sm">
                    <span className="text-slate-200 font-semibold text-base">
                      Serial Number
                    </span>
                    <input
                      type="text"
                      value={form.serialNumber}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          serialNumber: event.target.value,
                        }))
                      }
                      placeholder="e.g., SN-001122 or IMEI 123456789"
                      className="rounded-lg bg-slate-900/50 px-4 py-3 text-slate-100 placeholder:text-slate-600 transition-all duration-200 ease-out focus:outline-none focus:ring-2 focus:ring-brand/40 focus:bg-slate-900/70"
                    />
                    {submitAttempted && errors.serialNumber && (
                      <p className="text-sm text-status-stolen font-medium">
                        {errors.serialNumber}
                      </p>
                    )}
                  </label>

                  <label className="flex flex-col gap-3 text-sm">
                    <span className="text-slate-200 font-semibold text-base">
                      Item Model
                    </span>
                    <input
                      type="text"
                      value={form.itemModel}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          itemModel: event.target.value,
                        }))
                      }
                      placeholder="e.g., MacBook Pro 14-inch M2 Max"
                      className="rounded-lg bg-slate-900/50 px-4 py-3 text-slate-100 placeholder:text-slate-600 transition-all duration-200 ease-out focus:outline-none focus:ring-2 focus:ring-brand/40 focus:bg-slate-900/70"
                    />
                    {submitAttempted && errors.itemModel && (
                      <p className="text-sm text-status-stolen font-medium">
                        {errors.itemModel}
                      </p>
                    )}
                  </label>
                </div>
              </div>

              {/* Description Section */}
              <div className="bg-slate-800/40 backdrop-blur-sm border border-slate-700/50 rounded-2xl p-6 sm:p-8">
                <h2 className="text-2xl font-bold text-white mb-7 flex items-center gap-3">
                  <svg
                    className="w-6 h-6 text-brand flex-shrink-0"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                    />
                  </svg>
                  Item Description
                </h2>
                <label className="flex flex-col gap-3 text-sm">
                  <span className="text-slate-200 font-semibold text-base">
                    Details & Condition
                  </span>
                  <textarea
                    rows={5}
                    value={form.description}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        description: event.target.value,
                      }))
                    }
                    placeholder="Include identifying marks, visible damage, accessories, color, and any other distinguishing features. E.g., 'Silver with dent on left corner, includes original charger'"
                    className="rounded-lg bg-slate-900/50 px-4 py-3 text-slate-100 placeholder:text-slate-600 transition-all duration-200 ease-out focus:outline-none focus:ring-2 focus:ring-brand/40 focus:bg-slate-900/70 resize-none"
                  />
                  {submitAttempted && errors.description && (
                    <p className="text-sm text-status-stolen font-medium">
                      {errors.description}
                    </p>
                  )}
                </label>
              </div>

              {/* Evidence Section */}
              <div className="bg-slate-800/40 backdrop-blur-sm border border-slate-700/50 rounded-2xl p-6 sm:p-8">
                <h2 className="text-2xl font-bold text-white mb-7 flex items-center gap-3">
                  <svg
                    className="w-6 h-6 text-brand flex-shrink-0"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                    />
                  </svg>
                  Supporting Evidence
                </h2>
                <label className="flex flex-col gap-3 text-sm">
                  <span className="text-slate-200 font-semibold text-base">
                    Upload Image or PDF
                  </span>
                  <div className="relative">
                    <input
                      type="file"
                      accept="image/*,.pdf"
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          file: event.target.files?.[0] ?? null,
                        }))
                      }
                      className="absolute inset-0 opacity-0 cursor-pointer"
                    />
                    {!form.file && (
                      <div className="rounded-lg border-2 border-dashed border-brand/40 bg-brand/5 hover:bg-brand/10 hover:border-brand/60 transition-all duration-200 ease-out p-6 text-center cursor-pointer">
                        <div className="flex items-center justify-center mb-3">
                          <svg
                            className="w-8 h-8 text-brand"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
                            />
                          </svg>
                        </div>
                        <p className="text-sm font-semibold text-slate-200">
                          Click to upload or drag and drop
                        </p>
                        <p className="text-xs text-slate-400 mt-1">
                          PNG, JPG, or PDF (Max 10MB)
                        </p>
                      </div>
                    )}
                    {form.file && (
                      <div className="rounded-lg bg-gradient-to-r from-brand/15 to-brand/5 border border-brand/40 px-5 py-4 flex items-center gap-4 hover:from-brand/20 hover:to-brand/10 transition-all duration-200">
                        <div className="w-12 h-12 rounded-lg bg-gradient-to-br from-brand/40 to-brand/20 flex items-center justify-center flex-shrink-0">
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
                              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                            />
                          </svg>
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-semibold text-slate-100 truncate">
                            {form.file.name}
                          </p>
                          <p className="text-xs text-slate-400">
                            {(form.file.size / 1024 / 1024).toFixed(2)} MB •
                            Ready to submit
                          </p>
                        </div>
                        <svg
                          className="w-5 h-5 text-status-clean flex-shrink-0"
                          fill="currentColor"
                          viewBox="0 0 20 20"
                        >
                          <path
                            fillRule="evenodd"
                            d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                            clipRule="evenodd"
                          />
                        </svg>
                      </div>
                    )}
                  </div>
                  <p className="text-xs text-slate-500 mt-2">
                    Add photos of the item or documentation to help verify your
                    claim.
                  </p>
                  {submitAttempted && errors.file && (
                    <p className="text-sm text-status-stolen font-medium">
                      {errors.file}
                    </p>
                  )}
                </label>
              </div>

              {/* Feedback Section */}
              {feedback && (
                <div
                  className={`rounded-xl px-4 py-4 text-sm flex items-start gap-3 ${
                    feedback.type === "error"
                      ? "bg-status-stolen/15 text-status-stolen border border-status-stolen/30"
                      : "bg-status-clean/15 text-status-clean border border-status-clean/30"
                  }`}
                >
                  <span className="mt-0.5">
                    {feedback.type === "error" ? "✕" : "✓"}
                  </span>
                  <p>{feedback.message}</p>
                </div>
              )}

              {/* Action Buttons */}
              <div className="flex justify-end pt-8 border-t border-slate-700/30">
                <button
                  type="submit"
                  disabled={!canSubmit}
                  className={`inline-flex items-center justify-center gap-2 rounded-full bg-[color-mix(in_srgb,var(--color-brand)_88%,black)] px-8 py-3 text-base font-semibold text-white transition-all duration-200 ease-out hover:bg-[color-mix(in_srgb,var(--color-brand)_82%,black)] hover:text-white active:scale-[0.96] disabled:cursor-not-allowed disabled:opacity-40 disabled:bg-slate-700 ${
                    isSubmitting ? "opacity-80" : ""
                  }`}
                >
                  {isSubmitting ? (
                    <>
                      <svg
                        className="w-4 h-4 animate-spin"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"
                        />
                      </svg>
                      <span>Submitting...</span>
                    </>
                  ) : (
                    <>
                      <span>Submit Report</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </section>
      </main>
    </div>
  );
}
