"use client";

import { useEffect, useState, useMemo } from "react";
import { useRouter } from "next/navigation";
import { getAuthRole, getJwt } from "@/shared/auth";
import { fetchPendingReports, ReportAdmin, updateReportStatus } from "@/features/admin/lib/admin";
import { Pagination } from "@/features/shared/components/pagination";
import { Modal } from "@/features/shared/components/modal";

const ITEMS_PER_PAGE = 8;

function inferEvidenceType(
  fileType: string | null | undefined,
  fileUrl?: string | null,
): "IMAGE" | "PDF" {
  if (fileType === "PDF") return "PDF";
  if (fileType === "IMAGE") return "IMAGE";
  if (fileUrl && /\.pdf(\?|$)/i.test(fileUrl)) return "PDF";
  return "IMAGE";
}

export default function ModerationPage() {
  const router = useRouter();
  const [reports, setReports] = useState<ReportAdmin[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<number | null>(null);
  
  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  
  // Modal state
  const [selectedReport, setSelectedReport] = useState<ReportAdmin | null>(null);
  const [confirmAction, setConfirmAction] = useState<{ id: number; status: "APPROVED" | "REJECTED" } | null>(null);
  const [rejectionReason, setRejectionReason] = useState("");
  const [viewerFile, setViewerFile] = useState<{
    url: string;
    type: "IMAGE" | "PDF";
  } | null>(null);

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

  useEffect(() => {
    const token = getJwt();
    const role = getAuthRole();

    if (!token || role !== "ADMIN") {
      router.replace("/login");
      return;
    }

    loadReports();
  }, [router]);

  async function loadReports() {
    setIsLoading(true);
    try {
      const data = await fetchPendingReports();
      setReports(data);
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  }

  async function handleStatusChange(id: number, status: "APPROVED" | "REJECTED") {
    const trimmedReason = rejectionReason.trim();
    if (status === "REJECTED" && !trimmedReason) {
      return;
    }

    setActionLoading(id);
    try {
      await updateReportStatus(id, status, status === "REJECTED" ? trimmedReason : undefined);
      setReports((prev) => prev.filter((r) => r.id !== id));
      setSelectedReport(null);
      setConfirmAction(null);
      setRejectionReason("");
      // Adjust pagination if needed
      const remainingOnPage = paginatedReports.length - 1;
      if (remainingOnPage === 0 && currentPage > 1) {
        setCurrentPage((p) => p - 1);
      }
    } catch (error) {
      console.error(error);
      alert("Failed to update status");
    } finally {
      setActionLoading(null);
    }
  }

  const totalPages = Math.ceil(reports.length / ITEMS_PER_PAGE);
  const paginatedReports = useMemo(() => {
    const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
    return reports.slice(startIndex, startIndex + ITEMS_PER_PAGE);
  }, [reports, currentPage]);

  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

  return (
    <div className="min-h-screen text-slate-200">
      <main className="mx-auto w-full max-w-6xl px-4 pb-16 pt-32 sm:px-6 sm:pt-36 lg:px-8">
        
        <div className="mb-10">
          <h1 className="text-3xl font-extrabold text-white tracking-tight">Report Moderation</h1>
          <p className="mt-2 text-slate-400">Review, verify, and moderate reported items submitted by users.</p>
        </div>

        {isLoading ? (
          <div className="py-20 flex flex-col items-center justify-center">
            <div className="w-8 h-8 border-2 border-brand/30 border-t-brand rounded-full animate-spin mb-4" />
            <p className="text-sm text-slate-400">Loading pending reports...</p>
          </div>
        ) : reports.length === 0 ? (
          <div className="py-24 text-center border border-dashed border-white/10 rounded-3xl bg-[#0a1628]/30 backdrop-blur-sm">
            <div className="mx-auto w-12 h-12 text-slate-600 mb-4 flex items-center justify-center">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" className="w-8 h-8 text-brand/50"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M5 13l4 4L19 7"/></svg>
            </div>
            <h3 className="text-lg font-bold text-white mb-2">All caught up!</h3>
            <p className="text-sm text-slate-400">There are no pending reports to moderate.</p>
          </div>
        ) : (
          <div className="overflow-hidden rounded-3xl border border-slate-700/50 bg-[#0a1628]/40 shadow-lg backdrop-blur-xl">
            <table className="min-w-full divide-y divide-slate-700/50 text-left text-sm">
              <thead className="bg-slate-900/50">
                <tr>
                  <th scope="col" className="px-6 py-4 font-semibold text-slate-300">Item Details</th>
                  <th scope="col" className="px-6 py-4 font-semibold text-slate-300 hidden md:table-cell">Reported By</th>
                  <th scope="col" className="px-6 py-4 font-semibold text-slate-300">Date</th>
                  <th scope="col" className="px-6 py-4 font-semibold text-right text-slate-300"><span className="sr-only">Actions</span></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-700/50">
                {paginatedReports.map((report) => (
                  <tr 
                    key={report.id} 
                    className="hover:bg-slate-800/30 transition-colors cursor-pointer group"
                    onClick={() => setSelectedReport(report)}
                  >
                    <td className="px-6 py-4">
                      <div className="font-bold text-white group-hover:text-brand transition-colors">{report.itemModel}</div>
                      <div className="mt-1 text-xs text-slate-400 font-mono tracking-wider">SN: {report.serialNumber}</div>
                    </td>
                    <td className="px-6 py-4 hidden md:table-cell">
                      <div className="text-slate-200 font-medium">{report.ownerName || "Unknown"}</div>
                      <div className="text-slate-400 text-xs">{report.ownerEmail}</div>
                    </td>
                    <td className="px-6 py-4 text-slate-300 whitespace-nowrap">
                      {new Date(report.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setSelectedReport(report);
                        }}
                        className="inline-flex items-center justify-center rounded-lg bg-slate-800 border border-slate-700 px-3 py-1.5 text-xs font-semibold text-slate-300 transition-colors hover:bg-slate-700 hover:text-white"
                      >
                         Review
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            
            <Pagination 
              currentPage={currentPage} 
              totalPages={totalPages} 
              totalItems={reports.length}
              itemsPerPage={ITEMS_PER_PAGE}
              onPageChange={setCurrentPage} 
            />
          </div>
        )}

        {/* Details Modal */}
        <Modal
          isOpen={!!selectedReport && !confirmAction}
          onClose={() => setSelectedReport(null)}
          title="Review Report"
        >
          {selectedReport && (() => {
            return (
              <div className="space-y-6">
                <div className="flex gap-6 flex-col md:flex-row">
                  {/* Evidence section */}
                  <div className="w-full md:w-1/2 flex flex-col gap-3">
                    <p className="text-xs text-slate-500 font-semibold uppercase tracking-wider">Uploaded Evidence</p>
                    {!selectedReport.files || selectedReport.files.length === 0 ? (
                      <div className="rounded-2xl bg-black/40 flex flex-col items-center justify-center border border-slate-700/50 p-10 h-full min-h-[200px]">
                        <svg className="h-10 w-10 text-slate-600 mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        <span className="text-xs font-medium uppercase tracking-widest text-slate-500">No Evidence</span>
                      </div>
                    ) : (
                      <div className="grid grid-cols-2 gap-3 h-full max-h-[300px] overflow-y-auto pr-2 custom-scrollbar">
                        {selectedReport.files.map((file, idx) => {
                          const type = inferEvidenceType(file.fileType, file.fileUrl);
                          const fullUrl = `${backendUrl}${file.fileUrl}`;
                          return (
                            <button
                              key={file.id || idx}
                              type="button"
                              onClick={() => setViewerFile({ url: fullUrl, type })}
                              className={`group relative flex flex-col items-center justify-center overflow-hidden rounded-xl border border-slate-700/60 bg-slate-900/50 transition-all hover:border-brand/40 hover:bg-slate-800 ${
                                selectedReport.files.length === 1 ? 'col-span-2 aspect-square' : 'aspect-square'
                              }`}
                            >
                              {type === "PDF" ? (
                                <div className="flex flex-col items-center gap-2 text-slate-400 group-hover:text-slate-200 transition-colors">
                                  <svg className="h-8 w-8 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                                  </svg>
                                  <span className="text-xs font-semibold">View PDF</span>
                                </div>
                              ) : (
                                <>
                                  <img src={fullUrl} alt="Evidence preview" className="absolute inset-0 h-full w-full object-cover opacity-70 transition-opacity group-hover:opacity-40" />
                                  <div className="absolute inset-0 flex items-center justify-center bg-black/20 opacity-0 transition-opacity group-hover:opacity-100">
                                    <span className="rounded-lg bg-black/60 px-3 py-1.5 text-xs font-semibold text-white backdrop-blur-sm shadow-lg border border-white/10">View</span>
                                  </div>
                                </>
                              )}
                            </button>
                          );
                        })}
                      </div>
                    )}
                  </div>
                  
                  {/* Details section */}
                  <div className="w-full md:w-1/2 space-y-4">
                    <div>
                      <h3 className="text-2xl font-bold text-white">{selectedReport.itemModel}</h3>
                      <div className="inline-flex mt-2 text-xs font-mono text-slate-300 bg-slate-900 px-2 py-1 rounded border border-slate-700">
                        SN: {selectedReport.serialNumber}
                      </div>
                    </div>
                    
                    <div className="bg-slate-900/50 p-4 rounded-xl border border-slate-700/50">
                       <p className="text-xs text-slate-500 font-semibold uppercase tracking-wider mb-2">Description</p>
                       <p className="text-sm text-slate-300 leading-relaxed">{selectedReport.description}</p>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                       <div className="bg-slate-900/50 p-3 rounded-xl border border-slate-700/50">
                          <p className="text-xs text-slate-500 font-semibold uppercase tracking-wider mb-1">Reported By</p>
                          <p className="text-sm font-medium text-slate-200 truncate">{selectedReport.ownerName || "Unknown"}</p>
                          <p className="text-xs text-slate-400 truncate">{selectedReport.ownerEmail}</p>
                       </div>
                       <div className="bg-slate-900/50 p-3 rounded-xl border border-slate-700/50">
                          <p className="text-xs text-slate-500 font-semibold uppercase tracking-wider mb-1">Date Submitted</p>
                          <p className="text-sm font-medium text-slate-200">{new Date(selectedReport.createdAt).toLocaleDateString()}</p>
                       </div>
                    </div>
                  </div>
                </div>

                <div className="flex gap-3 pt-4 border-t border-slate-700/50 mt-6">
                  <button
                    onClick={() => setConfirmAction({ id: selectedReport.id, status: "REJECTED" })}
                    disabled={actionLoading !== null}
                    className="flex-1 rounded-xl bg-red-500/20 px-4 py-3 text-sm font-semibold text-red-400 hover:bg-red-500/30 active:scale-95 transition-all border border-red-500/30 disabled:opacity-50"
                  >
                     Reject
                  </button>
                  <button
                    onClick={() => setConfirmAction({ id: selectedReport.id, status: "APPROVED" })}
                    disabled={actionLoading !== null}
                    className="flex-1 rounded-xl bg-emerald-500/20 px-4 py-3 text-sm font-semibold text-emerald-400 hover:bg-emerald-500/30 active:scale-95 transition-all border border-emerald-500/30 disabled:opacity-50"
                  >
                    Approve Report
                  </button>
                </div>
              </div>
            );
          })()}
        </Modal>

        {/* Confirmation Modal */}
        <Modal
          isOpen={!!confirmAction}
          onClose={() => {
            setConfirmAction(null);
            setRejectionReason("");
          }}
          title="Confirm Action"
          maxWidth="max-w-md"
        >
          <div className="space-y-6">
            <p className="text-sm text-slate-300">
              Are you sure you want to <strong className={confirmAction?.status === "APPROVED" ? "text-emerald-400" : "text-red-400"}>{confirmAction?.status === "APPROVED" ? "approve" : "reject"}</strong> this report? This action cannot be undone.
            </p>
            {confirmAction?.status === "REJECTED" && (
              <div>
                <label htmlFor="report-rejection-reason" className="mb-2 block text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Rejection reason
                </label>
                <textarea
                  id="report-rejection-reason"
                  value={rejectionReason}
                  onChange={(event) => setRejectionReason(event.target.value)}
                  disabled={actionLoading !== null}
                  rows={4}
                  className="w-full resize-none rounded-xl border border-slate-700 bg-slate-950/70 px-3 py-2 text-sm text-slate-200 outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/20 disabled:opacity-50"
                  placeholder="Explain why this report is being rejected..."
                />
              </div>
            )}
            <div className="flex gap-3">
              <button
                onClick={() => {
                  setConfirmAction(null);
                  setRejectionReason("");
                }}
                disabled={actionLoading !== null}
                className="flex-1 rounded-xl bg-slate-800 px-4 py-2.5 text-sm font-semibold text-slate-300 hover:bg-slate-700 active:scale-95 transition-all border border-slate-700 disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                onClick={() => confirmAction && handleStatusChange(confirmAction.id, confirmAction.status)}
                disabled={actionLoading !== null || (confirmAction?.status === "REJECTED" && !rejectionReason.trim())}
                className={`flex-1 rounded-xl px-4 py-2.5 text-sm font-semibold text-white shadow-lg active:scale-95 transition-all disabled:opacity-50 ${
                  confirmAction?.status === "APPROVED" 
                    ? "bg-emerald-600 hover:bg-emerald-500 shadow-emerald-900/20" 
                    : "bg-red-600 hover:bg-red-500 shadow-red-900/20"
                }`}
              >
                {actionLoading !== null ? "Processing..." : "Confirm"}
              </button>
            </div>
          </div>
        </Modal>

      </main>

      {viewerFile && (
        <div
          className="fixed inset-0 z-[100] flex items-center justify-center bg-slate-950/85 p-4 backdrop-blur"
          role="dialog"
          aria-modal="true"
          onClick={() => setViewerFile(null)}
        >
          <div
            className="relative w-full max-w-5xl rounded-2xl border border-slate-700 bg-slate-900 shadow-2xl"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="flex items-center justify-between border-b border-slate-700 px-4 py-3">
              <h3 className="text-sm font-semibold uppercase tracking-[0.15em] text-slate-200">
                Evidence Viewer
              </h3>
              <button
                type="button"
                onClick={() => setViewerFile(null)}
                className="rounded-md border border-slate-600 px-2.5 py-1 text-xs font-semibold text-slate-300 hover:bg-slate-800"
              >
                Close
              </button>
            </div>
            <div className="max-h-[80vh] overflow-auto p-4">
              {viewerFile.type === "PDF" ? (
                <iframe
                  src={viewerFile.url}
                  title="Evidence PDF"
                  className="h-[75vh] w-full rounded-lg border border-slate-700"
                />
              ) : (
                <img
                  src={viewerFile.url}
                  alt="Evidence"
                  className="mx-auto h-auto max-h-[75vh] w-auto rounded-lg border border-slate-700"
                />
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
