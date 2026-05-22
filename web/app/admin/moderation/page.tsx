"use client";

import { useEffect, useState, useMemo } from "react";
import { useRouter } from "next/navigation";
import { getAuthRole, getJwt } from "@/shared/auth";
import { fetchPendingReports, ReportAdmin, updateReportStatus } from "@/features/admin/lib/admin";
import { Pagination } from "@/features/shared/components/pagination";
import { Modal } from "@/features/shared/components/modal";

const ITEMS_PER_PAGE = 8;

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
    setActionLoading(id);
    try {
      await updateReportStatus(id, status);
      setReports((prev) => prev.filter((r) => r.id !== id));
      setSelectedReport(null);
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
            const imageFile = selectedReport.files.find(f => f.fileType === "IMAGE");
            const fileUrl = imageFile ? `${backendUrl}${imageFile.fileUrl}` : null;
            
            return (
              <div className="space-y-6">
                <div className="flex gap-6 flex-col md:flex-row">
                  {/* Image section */}
                  <div className="w-full md:w-1/2 rounded-2xl overflow-hidden bg-black flex items-center justify-center border border-slate-700/50 aspect-square">
                    {fileUrl ? (
                      <img src={fileUrl} alt={selectedReport.itemModel} className="w-full h-full object-contain" />
                    ) : (
                      <div className="text-slate-600 flex flex-col items-center gap-2">
                        <svg className="h-10 w-10" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        <span className="text-xs font-medium uppercase tracking-widest">No Image</span>
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
          onClose={() => setConfirmAction(null)}
          title="Confirm Action"
          maxWidth="max-w-sm"
        >
          <div className="space-y-6">
            <p className="text-sm text-slate-300">
              Are you sure you want to <strong className={confirmAction?.status === "APPROVED" ? "text-emerald-400" : "text-red-400"}>{confirmAction?.status === "APPROVED" ? "approve" : "reject"}</strong> this report? This action cannot be undone.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setConfirmAction(null)}
                disabled={actionLoading !== null}
                className="flex-1 rounded-xl bg-slate-800 px-4 py-2.5 text-sm font-semibold text-slate-300 hover:bg-slate-700 active:scale-95 transition-all border border-slate-700 disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                onClick={() => confirmAction && handleStatusChange(confirmAction.id, confirmAction.status)}
                disabled={actionLoading !== null}
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
    </div>
  );
}
