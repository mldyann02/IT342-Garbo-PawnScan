"use client";

import Link from "next/link";
import { Suspense, useEffect, useMemo, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import UserDashboardHeader from "@/components/user-dashboard-header";
import { getAuthUser, getJwt } from "@/lib/auth";
import {
  deleteReport,
  fetchReports,
  updateReport,
  Report,
} from "@/lib/reports";

function formatDate(value: string): string {
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleString();
}

function ReportsPageContent() {
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

  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState<{
    serialNumber: string;
    itemModel: string;
    description: string;
    file: File | null;
  }>({
    serialNumber: "",
    itemModel: "",
    description: "",
    file: null,
  });
  const [isSaving, setIsSaving] = useState(false);

  const selectedReport = useMemo(
    () =>
      reports.find((report) => report.id === selectedReportId) ??
      reports[0] ??
      null,
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

    if (!token) {
      setIsLoading(false);
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
        if (
          error instanceof Error &&
          error.message.toLowerCase().includes("unauthorized")
        ) {
          setIsLoading(false);
          router.replace("/login");
          return;
        }
        setMessage({
          type: "error",
          text:
            error instanceof Error ? error.message : "Failed to load reports",
        });
      } finally {
        setIsLoading(false);
      }
    }

    loadReports();
  }, [router]);

  useEffect(() => {
    setIsEditing(false);
  }, [selectedReportId]);

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
        text:
          error instanceof Error ? error.message : "Failed to delete report",
      });
    } finally {
      setDeletingId(null);
    }
  }

  function handleEditClick() {
    if (!selectedReport) return;
    setEditForm({
      serialNumber: selectedReport.serialNumber,
      itemModel: selectedReport.itemModel,
      description: selectedReport.description,
      file: null,
    });
    setIsEditing(true);
  }

  async function handleSaveEdit() {
    if (!selectedReport) return;
    if (
      !editForm.serialNumber.trim() ||
      !editForm.itemModel.trim() ||
      !editForm.description.trim()
    ) {
      setMessage({ type: "error", text: "All fields are required." });
      return;
    }

    setIsSaving(true);
    setMessage(null);
    try {
      const payload: any = {
        serialNumber: editForm.serialNumber,
        itemModel: editForm.itemModel,
        description: editForm.description,
      };
      if (editForm.file) {
        payload.file = editForm.file;
      }

      const updated = await updateReport(selectedReport.id, payload);
      setReports((current) =>
        current.map((r) => (r.id === updated.id ? updated : r)),
      );
      setIsEditing(false);
      setMessage({ type: "success", text: "Report updated successfully." });
    } catch (error) {
      setMessage({
        type: "error",
        text:
          error instanceof Error ? error.message : "Failed to update report",
      });
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <div className="min-h-screen bg-bg-main relative overflow-hidden text-slate-200">
      {/* Decorative blurred background */}
      <div className="absolute inset-0 z-0 pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-brand/10 rounded-full blur-[120px]" />
        <div className="absolute bottom-1/4 right-1/4 w-[30rem] h-[30rem] bg-brand/5 rounded-full blur-[150px]" />
      </div>

      <UserDashboardHeader />

      <main className="relative z-10 mx-auto w-full max-w-6xl px-4 pb-16 pt-32 sm:px-6 lg:px-8">
        <div className="bg-[#0a1628]/80 backdrop-blur-xl border border-white/5 rounded-3xl p-6 sm:p-10 shadow-2xl">
          <div className="flex flex-wrap items-start justify-between gap-4 mb-8">
            <div>
              <h1 className="text-3xl font-extrabold text-white sm:text-4xl tracking-tight">
                Your Reports
              </h1>
              <p className="mt-3 text-sm text-slate-400">
                Manage your reported stolen items and keep records up to date.
              </p>
            </div>

            <Link
              href="/reports/create"
              className="w-full rounded-xl bg-brand/10 border border-brand/30 px-6 py-3 text-center text-sm font-semibold text-brand transition-all duration-200 ease-out hover:bg-brand/20 hover:border-brand/50 active:scale-[0.98] sm:w-auto flex items-center justify-center"
            >
              New Report
            </Link>
          </div>

          {(statusMessage || message) && (
            <div
              className={`mb-8 rounded-xl border p-4 text-sm ${
                message?.type === "error"
                  ? "border-status-stolen/30 bg-status-stolen/5 text-status-stolen"
                  : "border-brand/30 bg-brand/5 text-brand"
              }`}
            >
              <div className="flex gap-3">
                <svg
                  className="w-5 h-5 flex-shrink-0 mt-0.5"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    fillRule="evenodd"
                    d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                    clipRule="evenodd"
                  />
                </svg>
                {message?.text || statusMessage}
              </div>
            </div>
          )}

          {isLoading && (
            <div className="rounded-2xl border border-slate-700/50 bg-slate-800/40 p-8 text-center text-sm text-slate-400">
              <div className="mx-auto w-8 h-8 border-4 border-slate-600 border-t-brand rounded-full animate-spin mb-4" />
              Loading your reports...
            </div>
          )}

          {!isLoading && reports.length === 0 && (
            <div className="rounded-2xl border border-slate-700/50 bg-slate-800/40 p-12 text-center text-sm text-slate-400">
              <div className="mx-auto w-12 h-12 text-slate-500 mb-4">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={1.5}
                    d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                  />
                </svg>
              </div>
              <p className="text-base text-slate-300 mb-2">
                No reports found yet
              </p>
              <p>Create your first report to start tracking stolen items.</p>
            </div>
          )}

          {!isLoading && reports.length > 0 && (
            <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1.1fr_1.6fr] lg:items-start">
              <aside className="rounded-2xl border border-slate-700/50 bg-slate-800/40 p-4 shadow-xl flex flex-col lg:sticky lg:top-8 lg:max-h-[calc(100vh-6rem)]">
                <h2 className="px-2 pb-4 text-sm font-semibold text-slate-300 flex items-center gap-2 shrink-0">
                  <svg
                    className="w-4 h-4 text-brand"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M4 6h16M4 10h16M4 14h16M4 18h16"
                    />
                  </svg>
                  Previews
                </h2>
                <div className="space-y-2.5 overflow-y-auto pr-2 custom-scrollbar lg:max-h-[calc(100vh-12rem)]">
                  {reports.map((report) => {
                    const isActive = selectedReport?.id === report.id;
                    return (
                      <button
                        key={report.id}
                        type="button"
                        onClick={() => setSelectedReportId(report.id)}
                        className={`w-full group rounded-xl border p-4 text-left transition-all duration-200 ${
                          isActive
                            ? "border-brand/40 bg-brand/10 shadow-[0_0_15px_rgba(var(--color-brand),0.06)]"
                            : "border-slate-700/50 bg-slate-900/40 hover:border-slate-500/50 hover:bg-slate-800/60"
                        }`}
                      >
                        <div className="flex justify-between items-start mb-2">
                          <p
                            className={`text-sm font-bold truncate pr-3 ${isActive ? "text-brand" : "text-slate-200 group-hover:text-white"}`}
                          >
                            {report.serialNumber}
                          </p>
                          <span className="text-xs text-slate-500 whitespace-nowrap mt-0.5">
                            {formatDate(report.createdAt).split(",")[0]}
                          </span>
                        </div>
                        <p className="text-xs text-slate-400 line-clamp-1">
                          {report.itemModel}
                        </p>
                      </button>
                    );
                  })}
                </div>
              </aside>

              {selectedReport && (
                <article className="rounded-2xl border border-slate-700/50 bg-slate-800/40 p-6 sm:p-8 shadow-xl flex flex-col relative w-full">
                  {isEditing ? (
                    <div className="flex flex-col animate-in fade-in zoom-in-95 duration-200">
                      <div className="flex flex-wrap items-start justify-between gap-4 mb-6 pb-6 border-b border-slate-700/50 shrink-0">
                        <div>
                          <div className="-ml-1 flex items-center gap-2 mb-2">
                            <span className="-ml-2 px-2.5 py-1 rounded-md bg-brand/10 text-brand text-xs font-medium tracking-wide">
                              Edit mode
                            </span>
                          </div>
                          <h2 className="mt-2 text-2xl font-bold text-white">
                            Editing Report
                          </h2>
                        </div>
                      </div>

                      <div className="space-y-6">
                        <label className="flex flex-col gap-2">
                          <span className="text-sm font-semibold text-slate-400">
                            Serial Number
                          </span>
                          <input
                            type="text"
                            value={editForm.serialNumber}
                            onChange={(e) =>
                              setEditForm((c) => ({
                                ...c,
                                serialNumber: e.target.value,
                              }))
                            }
                            className="rounded-lg bg-slate-900/50 px-4 py-3 text-sm text-slate-100 focus:outline-none focus:ring-2 focus:ring-brand/40 focus:bg-slate-900/70 border border-slate-700/50"
                          />
                        </label>
                        <label className="flex flex-col gap-2">
                          <span className="text-sm font-semibold text-slate-400">
                            Item Model
                          </span>
                          <input
                            type="text"
                            value={editForm.itemModel}
                            onChange={(e) =>
                              setEditForm((c) => ({
                                ...c,
                                itemModel: e.target.value,
                              }))
                            }
                            className="rounded-lg bg-slate-900/50 px-4 py-3 text-sm text-slate-100 focus:outline-none focus:ring-2 focus:ring-brand/40 focus:bg-slate-900/70 border border-slate-700/50"
                          />
                        </label>
                        <label className="flex flex-col gap-2">
                          <span className="text-sm font-semibold text-slate-400">
                            Description
                          </span>
                          <textarea
                            rows={4}
                            value={editForm.description}
                            onChange={(e) =>
                              setEditForm((c) => ({
                                ...c,
                                description: e.target.value,
                              }))
                            }
                            className="rounded-lg bg-slate-900/50 px-4 py-3 text-sm text-slate-100 focus:outline-none focus:ring-2 focus:ring-brand/40 focus:bg-slate-900/70 border border-slate-700/50 resize-none custom-scrollbar"
                          />
                        </label>
                        <label className="flex flex-col gap-2">
                          <span className="text-sm font-semibold text-slate-400">
                            Update Evidence File (Optional)
                          </span>
                          <div className="relative rounded-xl border border-dashed border-slate-700/50 bg-slate-900/20 p-6 flex flex-col items-center justify-center text-center hover:bg-slate-900/40 transition-colors">
                            <input
                              type="file"
                              accept="image/*,application/pdf"
                              onChange={(e) =>
                                setEditForm((c) => ({
                                  ...c,
                                  file: e.target.files?.[0] || null,
                                }))
                              }
                              className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                            />
                            {editForm.file ? (
                              <div className="flex items-center gap-3">
                                <svg
                                  className="w-8 h-8 text-brand flex-shrink-0"
                                  fill="none"
                                  viewBox="0 0 24 24"
                                  stroke="currentColor"
                                >
                                  <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    strokeWidth={1.5}
                                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                                  />
                                </svg>
                                <div className="text-left overflow-hidden">
                                  <p className="text-sm font-medium text-slate-200 truncate max-w-[200px] sm:max-w-[300px]">
                                    {editForm.file.name}
                                  </p>
                                  <p className="text-xs text-slate-500 mt-0.5">
                                    Click or drag to replace
                                  </p>
                                </div>
                              </div>
                            ) : (
                              <>
                                <svg
                                  className="w-8 h-8 text-slate-500 mb-2"
                                  fill="none"
                                  stroke="currentColor"
                                  viewBox="0 0 24 24"
                                >
                                  <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    strokeWidth={1.5}
                                    d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                                  />
                                </svg>
                                <p className="text-sm text-slate-300 font-medium mb-1">
                                  Click to upload or drag and drop
                                </p>
                                <p className="text-xs text-slate-500">
                                  Leave blank to keep existing file
                                </p>
                              </>
                            )}
                          </div>
                        </label>
                      </div>

                      <div className="mt-8 pt-6 border-t border-slate-700/50 flex flex-wrap gap-4 items-center justify-end">
                        <button
                          type="button"
                          onClick={() => setIsEditing(false)}
                          disabled={isSaving}
                          className="rounded-xl bg-slate-800 border border-slate-600/50 px-6 py-2.5 text-sm font-semibold text-slate-200 transition-all hover:bg-slate-700 hover:border-slate-500 active:scale-95 disabled:opacity-50"
                        >
                          Cancel
                        </button>
                        <button
                          type="button"
                          onClick={handleSaveEdit}
                          disabled={isSaving}
                          className="rounded-xl border border-brand/30 bg-brand/10 px-6 py-2.5 text-sm font-semibold text-brand transition-all hover:bg-brand/20 hover:border-brand/50 active:scale-95 disabled:cursor-not-allowed disabled:opacity-50 flex items-center gap-2"
                        >
                          {isSaving ? (
                            <>
                              <div className="w-4 h-4 border-2 border-brand/30 border-t-brand rounded-full animate-spin" />
                              Saving...
                            </>
                          ) : (
                            "Save Changes"
                          )}
                        </button>
                      </div>
                    </div>
                  ) : (
                    <>
                      <div className="flex flex-wrap items-start justify-between gap-4 mb-6 pb-6 border-b border-slate-700/50 shrink-0">
                        <div>
                          <div className="-ml-3 flex items-center gap-2 mb-2">
                            <span className="px-2.5 py-1 rounded-md bg-brand/10 text-brand text-xs font-medium tracking-wide">
                              Report Details
                            </span>
                          </div>
                          <h2 className="mt-2 text-2xl font-bold text-white">
                            {selectedReport.serialNumber}
                          </h2>
                          <p className="mt-1.5 text-base text-slate-300">
                            {selectedReport.itemModel}
                          </p>
                        </div>
                        <div className="text-right flex flex-col justify-end">
                          <p className="text-xs font-medium text-slate-500">
                            Reported:{" "}
                            <span className="text-slate-400">
                              {formatDate(selectedReport.createdAt)}
                            </span>
                          </p>
                          {selectedReport.updatedAt &&
                            selectedReport.createdAt !==
                              selectedReport.updatedAt && (
                              <p className="text-xs font-medium text-slate-500 mt-1">
                                Edited:{" "}
                                <span className="text-slate-400">
                                  {formatDate(selectedReport.updatedAt)}
                                </span>
                              </p>
                            )}
                        </div>
                      </div>

                      <div className="space-y-6">
                        <div>
                          <h3 className="text-sm font-semibold text-slate-400 flex items-center gap-2 mb-3">
                            <svg
                              className="w-4 h-4"
                              fill="none"
                              stroke="currentColor"
                              viewBox="0 0 24 24"
                            >
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M4 6h16M4 12h16M4 18h7"
                              />
                            </svg>
                            Description
                          </h3>
                          <div className="rounded-xl border border-slate-700/30 bg-slate-900/30 p-4">
                            <p className="text-sm text-slate-300 leading-relaxed break-words whitespace-pre-wrap">
                              {selectedReport.description}
                            </p>
                          </div>
                        </div>

                        <div>
                          <h3 className="text-sm font-semibold text-slate-400 flex items-center gap-2 mb-3">
                            <svg
                              className="w-4 h-4"
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
                            Evidence / Uploaded File
                          </h3>

                          {selectedReport.files?.length ? (
                            <div className="grid grid-cols-1 gap-4">
                              {selectedReport.files.map((file) => {
                                const fileUrl = `${process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080"}${file.fileUrl}`;
                                const token = getJwt();
                                const previewUrl =
                                  file.fileType === "PDF"
                                    ? `/api/reports/file?path=${encodeURIComponent(file.fileUrl)}${token ? `&token=${encodeURIComponent(token)}` : ""}`
                                    : fileUrl;
                                return (
                                  <div
                                    key={file.id}
                                    className="group relative rounded-xl border border-slate-700/50 bg-slate-900/40 overflow-hidden"
                                  >
                                    {file.fileType === "IMAGE" ? (
                                      <div className="aspect-video w-full">
                                        <img
                                          src={fileUrl}
                                          alt="Uploaded evidence"
                                          className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
                                        />
                                        <div className="absolute inset-0 bg-slate-900/0 group-hover:bg-slate-900/30 transition-colors duration-300" />
                                      </div>
                                    ) : (
                                      <div className="aspect-video w-full bg-slate-800">
                                        <iframe
                                          title={`PDF first page ${file.id}`}
                                          src={`${previewUrl}#page=1&view=FitH&toolbar=0&navpanes=0&scrollbar=0`}
                                          className="h-full w-full"
                                        />
                                      </div>
                                    )}
                                    <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                                      <button
                                        type="button"
                                        onClick={() =>
                                          setViewerFile({
                                            url: previewUrl,
                                            type: file.fileType,
                                          })
                                        }
                                        className="rounded-lg bg-slate-900/90 backdrop-blur-md px-4 py-2.5 text-sm font-semibold text-white shadow-xl hover:bg-black transition-colors flex items-center gap-2"
                                      >
                                        <svg
                                          className="w-4 h-4"
                                          fill="none"
                                          stroke="currentColor"
                                          viewBox="0 0 24 24"
                                        >
                                          <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth={2}
                                            d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                                          />
                                          <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth={2}
                                            d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                                          />
                                        </svg>
                                        Preview
                                      </button>
                                    </div>
                                  </div>
                                );
                              })}
                            </div>
                          ) : (
                            <div className="rounded-xl border border-dashed border-slate-700/50 bg-slate-900/20 p-6 flex flex-col items-center justify-center text-center">
                              <svg
                                className="w-8 h-8 text-slate-600 mb-2"
                                fill="none"
                                stroke="currentColor"
                                viewBox="0 0 24 24"
                              >
                                <path
                                  strokeLinecap="round"
                                  strokeLinejoin="round"
                                  strokeWidth={1.5}
                                  d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                                />
                              </svg>
                              <p className="text-sm text-slate-500">
                                No evidence file was uploaded
                              </p>
                            </div>
                          )}
                        </div>
                      </div>

                      <div className="mt-8 pt-6 border-t border-slate-700/50 flex flex-wrap gap-4 items-center justify-end">
                        <button
                          type="button"
                          onClick={handleEditClick}
                          className="rounded-xl bg-slate-800 border border-slate-600/50 px-6 py-2.5 text-sm font-semibold text-slate-200 transition-all hover:bg-slate-700 hover:border-slate-500 active:scale-95 flex flex-row items-center justify-center gap-2"
                        >
                          <svg
                            className="w-4 h-4"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                            />
                          </svg>
                          Edit
                        </button>
                        <button
                          type="button"
                          onClick={() => handleDelete(selectedReport.id)}
                          disabled={deletingId === selectedReport.id}
                          className="rounded-xl border border-red-500/20 bg-red-500/10 px-6 py-2.5 text-sm font-semibold text-red-500 transition-all hover:bg-red-500/20 hover:border-red-500/30 active:scale-95 disabled:cursor-not-allowed disabled:opacity-50 disabled:active:scale-100 flex items-center justify-center gap-2"
                        >
                          {deletingId === selectedReport.id ? (
                            <>
                              <div className="w-4 h-4 border-2 border-red-500/30 border-t-red-500 rounded-full animate-spin" />
                              Deleting...
                            </>
                          ) : (
                            <>
                              <svg
                                className="w-4 h-4"
                                fill="none"
                                stroke="currentColor"
                                viewBox="0 0 24 24"
                              >
                                <path
                                  strokeLinecap="round"
                                  strokeLinejoin="round"
                                  strokeWidth={2}
                                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                                />
                              </svg>
                              Delete
                            </>
                          )}
                        </button>
                      </div>
                    </>
                  )}
                </article>
              )}
            </div>
          )}
        </div>
      </main>

      {/* Viewer Modal overlay */}
      {viewerFile && (
        <div
          className="fixed inset-0 z-[70] flex items-center justify-center bg-[#071022]/80 p-4 backdrop-blur-md transition-all"
          onClick={() => setViewerFile(null)}
        >
          <div
            className="w-full max-w-5xl rounded-2xl border border-white/10 bg-[#0a1628] p-5 shadow-2xl relative"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="mb-4 flex items-center justify-between border-b gap-4 border-slate-700/50 pb-4">
              <h3 className="text-lg font-bold text-white flex items-center gap-2">
                <svg
                  className="w-5 h-5 text-brand"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                  />
                </svg>
                Evidence Preview
              </h3>
              <button
                type="button"
                onClick={() => setViewerFile(null)}
                className="rounded-lg bg-slate-800 p-2 text-sm font-semibold text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
                title="Close preview"
              >
                <svg
                  className="w-5 h-5"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                </svg>
              </button>
            </div>

            <div className="bg-slate-900/50 rounded-xl overflow-hidden flex items-center justify-center p-2 relative">
              {viewerFile.type === "IMAGE" ? (
                <img
                  src={viewerFile.url}
                  alt="Uploaded evidence full preview"
                  className="max-h-[75vh] w-auto max-w-full rounded-lg object-contain"
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
        </div>
      )}
    </div>
  );
}

export default function ReportsPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen bg-gradient-to-b from-bg-main to-[#071022]" />
      }
    >
      <ReportsPageContent />
    </Suspense>
  );
}
