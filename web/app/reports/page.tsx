"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import UserDashboardHeader from "@/components/user-dashboard-header";
import { getAuthUser, getJwt } from "@/lib/auth";
import { deleteReport, fetchReports, Report } from "@/lib/reports";

function formatDate(value: string): string {
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleString();
}

export default function ReportsPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [reports, setReports] = useState<Report[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [selectedReportId, setSelectedReportId] = useState<number | null>(null);
  const [message, setMessage] = useState<{
    type: "success" | "error";
    text: string;
  } | null>(null);
  const [viewerFile, setViewerFile] = useState<{
    url: string;
    type: "IMAGE" | "PDF";
  } | null>(null);

  const selectedReport = useMemo(
    () => reports.find((report) => report.id === selectedReportId) ?? reports[0] ?? null,
    [reports, selectedReportId],
  );

  const statusMessage = useMemo(() => {
    if (searchParams.get("created") === "1") {
      return "Report created successfully.";
    }
    if (searchParams.get("updated") === "1") {
      return "Report updated successfully.";
    }
    return "";
  }, [searchParams]);

  useEffect(() => {
    const token = getJwt();
    const authenticatedEmail = getAuthUser();

    if (!token && !authenticatedEmail) {
      router.replace("/login");
      return;
    }

    async function loadReports() {
      setIsLoading(true);
      try {
        const data = await fetchReports();
        setReports(data);
        setSelectedReportId(data[0]?.id ?? null);
      } catch (error) {
        setMessage({
          type: "error",
          text: error instanceof Error ? error.message : "Failed to load reports",
        });
      } finally {
        setIsLoading(false);
      }
    }

    loadReports();
  }, [router]);

  useEffect(() => {
    function handleEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setViewerFile(null);
      }
    }

    if (viewerFile) {
      document.body.style.overflow = "hidden";
      document.addEventListener("keydown", handleEscape);
    }

    return () => {
      document.body.style.overflow = "";
      document.removeEventListener("keydown", handleEscape);
    };
  }, [viewerFile]);

  async function handleDelete(reportId: number) {
    const shouldDelete = window.confirm("Delete this report permanently?");
    if (!shouldDelete) {
      return;
    }

    setMessage(null);
    setDeletingId(reportId);

    try {
      await deleteReport(reportId);
      setReports((current) => {
        const next = current.filter((report) => report.id !== reportId);
        if (selectedReportId === reportId) {
          setSelectedReportId(next[0]?.id ?? null);
        }
        return next;
      });
      setMessage({ type: "success", text: "Report deleted successfully." });
    } catch (error) {
      setMessage({
        type: "error",
        text: error instanceof Error ? error.message : "Failed to delete report",
      });
    } finally {
      setDeletingId(null);
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-bg-main to-[#071022] text-slate-200">
      <UserDashboardHeader />

      <main className="mx-auto w-full max-w-6xl px-4 pb-16 pt-36 sm:px-6 sm:pt-40 md:pt-32 lg:px-8">
        <section className="glass-panel rounded-2xl bg-slate-900/35 p-6 shadow-[0_18px_36px_rgba(0,0,0,0.28)] sm:p-8">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand/90">
                My Reports
              </p>
              <h1 className="mt-2 text-2xl font-bold text-white sm:text-3xl">Your submitted reports</h1>
              <p className="mt-3 text-sm text-slate-300">
                Manage your reported stolen items and keep records up to date.
              </p>
            </div>

            <Link
              href="/reports/create"
              className="w-full rounded-full bg-brand px-5 py-2.5 text-center text-sm font-semibold text-bg-main transition-all duration-200 ease-out hover:brightness-90 active:scale-[0.98] sm:w-auto"
            >
              New Report
            </Link>
          </div>

          {(statusMessage || message) && (
            <div
              className={`mt-5 rounded-lg border px-4 py-3 text-sm ${
                message?.type === "error"
                  ? "border-status-stolen/40 bg-status-stolen/10 text-status-stolen"
                  : "border-status-clean/40 bg-status-clean/10 text-status-clean"
              }`}
            >
              {message?.text || statusMessage}
            </div>
          )}

          {isLoading && (
            <div className="mt-6 rounded-xl bg-slate-900/55 p-5 text-sm text-slate-300">
              Loading your reports...
            </div>
          )}

          {!isLoading && reports.length === 0 && (
            <div className="mt-6 rounded-xl bg-slate-900/55 p-5 text-sm text-slate-300">
              No reports found yet. Create your first report to start tracking stolen items.
            </div>
          )}

          {!isLoading && reports.length > 0 && (
            <div className="mt-6 grid grid-cols-1 gap-4 lg:grid-cols-[1.1fr_1.4fr]">
              <aside className="rounded-xl bg-slate-900/55 p-3 shadow-[0_6px_18px_rgba(0,0,0,0.18)]">
                <p className="px-2 pb-2 text-xs uppercase tracking-[0.12em] text-slate-400">Report previews</p>
                <div className="max-h-[34rem] space-y-2 overflow-y-auto pr-1">
                  {reports.map((report) => {
                    const isActive = selectedReport?.id === report.id;
                    return (
                      <button
                        key={report.id}
                        type="button"
                        onClick={() => setSelectedReportId(report.id)}
                        className={`w-full rounded-lg border px-3 py-3 text-left transition ${
                          isActive
                            ? "border-brand/60 bg-brand/15"
                            : "border-slate-700/60 bg-slate-900/60 hover:border-slate-500/70"
                        }`}
                      >
                        <p className="text-sm font-semibold text-white">{report.serialNumber}</p>
                        <p className="mt-1 text-xs text-slate-300">{report.itemModel}</p>
                        <p className="mt-1 text-xs text-slate-400">{formatDate(report.createdAt)}</p>
                      </button>
                    );
                  })}
                </div>
              </aside>

              {selectedReport && (
                <article className="rounded-xl bg-slate-900/60 p-5 shadow-[0_6px_18px_rgba(0,0,0,0.18)]">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <p className="text-xs uppercase tracking-[0.14em] text-brand">Selected report</p>
                      <h2 className="mt-1 text-xl font-semibold text-white">{selectedReport.serialNumber}</h2>
                      <p className="mt-1 text-sm text-slate-300">{selectedReport.itemModel}</p>
                    </div>
                    <p className="text-xs uppercase tracking-[0.12em] text-slate-400">
                      Reported {formatDate(selectedReport.createdAt)}
                    </p>
                  </div>

                  <div className="mt-4 rounded-lg bg-slate-950/45 p-4">
                    <p className="text-xs uppercase tracking-[0.12em] text-slate-400">Description</p>
                    <p className="mt-2 text-sm text-slate-200">{selectedReport.description}</p>
                  </div>

                  <div className="mt-4 rounded-lg bg-slate-950/45 p-4">
                    <p className="text-xs uppercase tracking-[0.12em] text-slate-400">Uploaded file preview</p>

                    {selectedReport.files?.length ? (
                      <div className="mt-3 space-y-3">
                        {selectedReport.files.map((file) => {
                          const fileUrl = `${process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080"}${file.fileUrl}`;
                          return (
                            <div key={file.id} className="rounded-lg border border-slate-700/70 bg-slate-900/70 p-3">
                              {file.fileType === "IMAGE" ? (
                                <img
                                  src={fileUrl}
                                  alt="Uploaded evidence"
                                  className="h-44 w-full rounded-md object-cover"
                                />
                              ) : (
                                <iframe
                                  title={`Attachment ${file.id}`}
                                  src={fileUrl}
                                  className="h-56 w-full rounded-md bg-slate-800"
                                />
                              )}
                              <button
                                type="button"
                                onClick={() => setViewerFile({ url: fileUrl, type: file.fileType })}
                                className="mt-2 inline-flex rounded-full bg-brand/15 px-3 py-1 text-xs font-semibold text-brand hover:bg-brand/25"
                              >
                                Open {file.fileType}
                              </button>
                            </div>
                          );
                        })}
                      </div>
                    ) : (
                      <p className="mt-2 text-sm text-slate-400">No file uploaded for this report.</p>
                    )}
                  </div>

                  <div className="mt-5 flex flex-wrap gap-3">
                    <Link
                      href={`/reports/edit/${selectedReport.id}`}
                      className="rounded-full bg-slate-700/75 px-4 py-2 text-sm font-semibold text-slate-100 transition hover:bg-slate-600/75"
                    >
                      Edit
                    </Link>
                    <button
                      type="button"
                      onClick={() => handleDelete(selectedReport.id)}
                      disabled={deletingId === selectedReport.id}
                      className="rounded-full bg-red-500/80 px-4 py-2 text-sm font-semibold text-white transition hover:bg-red-500 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                      {deletingId === selectedReport.id ? "Deleting..." : "Delete"}
                    </button>
                  </div>
                </article>
              )}
            </div>
          )}
        </section>
      </main>

      {viewerFile && (
        <div
          className="fixed inset-0 z-[70] flex items-center justify-center bg-black/45 p-4 backdrop-blur-sm"
          onClick={() => setViewerFile(null)}
        >
          <div
            className="w-full max-w-4xl rounded-2xl border border-slate-700/70 bg-slate-950/95 p-4 shadow-[0_20px_60px_rgba(0,0,0,0.45)]"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="mb-3 flex items-center justify-between">
              <p className="text-sm font-semibold text-slate-200">File Preview</p>
              <button
                type="button"
                onClick={() => setViewerFile(null)}
                className="rounded-full bg-slate-800 px-3 py-1.5 text-xs font-semibold text-slate-200 hover:bg-slate-700"
              >
                Close
              </button>
            </div>

            {viewerFile.type === "IMAGE" ? (
              <img
                src={viewerFile.url}
                alt="Uploaded evidence full preview"
                className="max-h-[75vh] w-full rounded-lg object-contain"
              />
            ) : (
              <iframe
                title="Uploaded PDF preview"
                src={viewerFile.url}
                className="h-[75vh] w-full rounded-lg bg-slate-900"
              />
            )}
          </div>
        </div>
      )}
    </div>
  );
}
