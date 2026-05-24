"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthRole, getJwt } from "@/shared/auth";
import { 
  AdminStats, 
  fetchAdminStats, 
  fetchPendingReports, 
  fetchAllBusinesses, 
  ReportAdmin, 
  BusinessProfileAdmin,
  updateReportStatus,
  rejectBusiness,
  verifyBusiness
} from "@/features/admin/lib/admin";
import Link from "next/link";
import { Modal } from "@/features/shared/components/modal";

export default function AdminDashboardPage() {
  const router = useRouter();
  const [stats, setStats] = useState<AdminStats | null>(null);
  
  // Recent items state
  const [recentReports, setRecentReports] = useState<ReportAdmin[]>([]);
  const [recentBusinesses, setRecentBusinesses] = useState<BusinessProfileAdmin[]>([]);
  
  const [isLoading, setIsLoading] = useState(true);

  // Modal states
  const [selectedReport, setSelectedReport] = useState<ReportAdmin | null>(null);
  const [confirmReportAction, setConfirmReportAction] = useState<{ id: number; status: "APPROVED" | "REJECTED" } | null>(null);
  const [reportRejectionReason, setReportRejectionReason] = useState("");
  const [selectedBusiness, setSelectedBusiness] = useState<BusinessProfileAdmin | null>(null);
  const [confirmBusinessAction, setConfirmBusinessAction] = useState<{ id: number; action: "APPROVE" | "REJECT" } | null>(null);
  const [businessRejectionReason, setBusinessRejectionReason] = useState("");
  const [actionLoading, setActionLoading] = useState<number | null>(null);

  useEffect(() => {
    const token = getJwt();
    const role = getAuthRole();

    if (!token || role !== "ADMIN") {
      router.replace("/login");
      return;
    }

    loadDashboardData();
  }, [router]);

  async function loadDashboardData() {
    setIsLoading(true);
    try {
      const [statsData, reportsData, businessesData] = await Promise.all([
        fetchAdminStats(),
        fetchPendingReports(),
        fetchAllBusinesses()
      ]);
      setStats(statsData);
      setRecentReports(reportsData.slice(0, 3));
      setRecentBusinesses(businessesData.filter(b => !b.isVerified && !b.isRejected).slice(0, 3));
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  }

  async function handleReportStatusChange(id: number, status: "APPROVED" | "REJECTED") {
    const trimmedReason = reportRejectionReason.trim();
    if (status === "REJECTED" && !trimmedReason) {
      return;
    }

    setActionLoading(id);
    try {
      await updateReportStatus(id, status, status === "REJECTED" ? trimmedReason : undefined);
      setRecentReports((prev) => prev.filter((r) => r.id !== id));
      setSelectedReport(null);
      setConfirmReportAction(null);
      setReportRejectionReason("");
      // Optional: Refresh stats
      fetchAdminStats().then(setStats).catch(console.error);
    } catch (error) {
      console.error(error);
      alert("Failed to update status");
    } finally {
      setActionLoading(null);
    }
  }

  async function handleBusinessVerify(id: number) {
    setActionLoading(id);
    try {
      await verifyBusiness(id);
      setRecentBusinesses((prev) => prev.filter(b => b.userId !== id));
      setSelectedBusiness(null);
      setConfirmBusinessAction(null);
      // Optional: Refresh stats
      fetchAdminStats().then(setStats).catch(console.error);
    } catch (error) {
      console.error(error);
      alert("Failed to verify business");
    } finally {
      setActionLoading(null);
    }
  }

  async function handleBusinessReject(id: number) {
    const trimmedReason = businessRejectionReason.trim();
    if (!trimmedReason) {
      return;
    }

    setActionLoading(id);
    try {
      await rejectBusiness(id, trimmedReason);
      setRecentBusinesses((prev) => prev.filter(b => b.userId !== id));
      setSelectedBusiness(null);
      setConfirmBusinessAction(null);
      setBusinessRejectionReason("");
      fetchAdminStats().then(setStats).catch(console.error);
    } catch (error) {
      console.error(error);
      alert("Failed to reject business");
    } finally {
      setActionLoading(null);
    }
  }

  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

  return (
    <div className="min-h-screen text-slate-200">
      <main className="mx-auto w-full max-w-5xl px-4 pb-16 pt-32 sm:px-6 sm:pt-36 lg:px-8">
        {/* Hero Section */}
        <section className="relative overflow-hidden rounded-3xl border-0 bg-[#0a1628]/80 p-8 sm:p-12 shadow-[0_24px_60px_rgba(0,0,0,0.4)] backdrop-blur-xl">
          <div className="absolute top-0 right-0 -mr-20 -mt-20 h-72 w-72 rounded-full bg-brand/10 blur-[80px] pointer-events-none" />

          <div className="relative z-10 max-w-2xl">
            <h1 className="text-4xl font-extrabold tracking-tight text-transparent bg-clip-text bg-gradient-to-br from-white via-slate-200 to-brand sm:text-5xl pb-1">
              Admin Overview
            </h1>

            <p className="mt-4 text-lg text-slate-400 font-light leading-relaxed max-w-xl">
              Monitor system integrity, moderate user reports, and manage business verifications all from one place.
            </p>
          </div>
        </section>

        {/* Stats Grid */}
        <section className="mt-12">
          <h2 className="text-2xl font-extrabold text-white tracking-tight mb-8">System Integrity Metrics</h2>
          
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
            {isLoading ? (
              <div className="col-span-full py-12 flex flex-col items-center justify-center">
                 <div className="w-8 h-8 border-2 border-brand/30 border-t-brand rounded-full animate-spin mb-4" />
                 <p className="text-sm text-slate-400">Loading metrics...</p>
              </div>
            ) : (
              <>
                <div className="relative overflow-hidden rounded-3xl border-0 bg-[#0a1628]/60 p-6 shadow-[0_8px_30px_rgba(0,0,0,0.2)] backdrop-blur-xl">
                  <p className="text-sm font-medium text-slate-400">Total Users</p>
                  <p className="mt-2 text-4xl font-bold text-white">{stats?.totalUsers || 0}</p>
                </div>
                
                <div className="relative overflow-hidden rounded-3xl border-0 bg-[#0a1628]/60 p-6 shadow-[0_8px_30px_rgba(0,0,0,0.2)] backdrop-blur-xl">
                  <p className="text-sm font-medium text-slate-400">Total Businesses</p>
                  <p className="mt-2 text-4xl font-bold text-white">{stats?.totalBusinesses || 0}</p>
                </div>

                <div className="relative overflow-hidden rounded-3xl border-0 bg-[#0a1628]/60 p-6 shadow-[0_8px_30px_rgba(0,0,0,0.2)] backdrop-blur-xl group">
                  <p className="text-sm font-medium text-brand group-hover:text-brand/80 transition-colors">Pending Reports</p>
                  <p className="mt-2 text-4xl font-bold text-white group-hover:text-slate-200 transition-colors">{stats?.pendingReports || 0}</p>
                  <Link href="/admin/moderation" className="absolute inset-0 z-10" aria-label="View Pending Reports" />
                </div>

                <div className="relative overflow-hidden rounded-3xl border-0 bg-[#0a1628]/60 p-6 shadow-[0_8px_30px_rgba(0,0,0,0.2)] backdrop-blur-xl group">
                  <p className="text-sm font-medium text-brand group-hover:text-brand/80 transition-colors">Pending Verification</p>
                  <p className="mt-2 text-4xl font-bold text-white group-hover:text-slate-200 transition-colors">{stats?.pendingBusinesses || 0}</p>
                  <Link href="/admin/users" className="absolute inset-0 z-10" aria-label="View Pending Businesses" />
                </div>
              </>
            )}
          </div>
        </section>

        {/* Recent Items Section */}
        {!isLoading && (
          <div className="mt-12 grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Recent Pending Reports */}
            <section className="overflow-hidden rounded-3xl border border-slate-700/50 bg-[#0a1628]/40 shadow-lg backdrop-blur-xl flex flex-col">
              <div className="flex items-center justify-between border-b border-slate-700/50 bg-slate-900/50 px-6 py-4">
                <h3 className="font-bold text-white">Recent Reports to Moderate</h3>
                <Link href="/admin/moderation" className="text-sm font-medium text-brand hover:text-brand/80 transition-colors">
                  View all &rarr;
                </Link>
              </div>
              <div className="flex-1 divide-y divide-slate-700/50">
                {recentReports.length === 0 ? (
                  <div className="p-8 text-center text-sm text-slate-400">
                    No pending reports. All caught up!
                  </div>
                ) : (
                  recentReports.map(report => (
                    <button 
                      key={report.id} 
                      onClick={() => setSelectedReport(report)}
                      className="w-full text-left p-6 hover:bg-slate-800/30 transition-colors group cursor-pointer"
                    >
                      <div className="flex justify-between items-start">
                        <div>
                          <p className="font-bold text-white group-hover:text-brand transition-colors">{report.itemModel}</p>
                          <p className="mt-1 text-xs text-slate-400">SN: {report.serialNumber}</p>
                        </div>
                        <span className="text-xs text-slate-500 whitespace-nowrap">{new Date(report.createdAt).toLocaleDateString()}</span>
                      </div>
                      <p className="mt-3 text-sm text-slate-300 line-clamp-2 leading-relaxed">{report.description}</p>
                    </button>
                  ))
                )}
              </div>
            </section>

            {/* Recent Pending Businesses */}
            <section className="overflow-hidden rounded-3xl border border-slate-700/50 bg-[#0a1628]/40 shadow-lg backdrop-blur-xl flex flex-col">
              <div className="flex items-center justify-between border-b border-slate-700/50 bg-slate-900/50 px-6 py-4">
                <h3 className="font-bold text-white">Recent Business Signups</h3>
                <Link href="/admin/users" className="text-sm font-medium text-brand hover:text-brand/80 transition-colors">
                  View all &rarr;
                </Link>
              </div>
              <div className="flex-1 divide-y divide-slate-700/50">
                {recentBusinesses.length === 0 ? (
                  <div className="p-8 text-center text-sm text-slate-400">
                    No pending verifications. All caught up!
                  </div>
                ) : (
                  recentBusinesses.map(business => (
                    <button 
                      key={business.userId} 
                      onClick={() => setSelectedBusiness(business)}
                      className="w-full text-left p-6 hover:bg-slate-800/30 transition-colors group cursor-pointer"
                    >
                      <div className="flex justify-between items-start">
                        <div>
                          <p className="font-bold text-white group-hover:text-brand transition-colors">{business.businessName}</p>
                          <p className="mt-1 text-xs text-slate-400">{business.ownerEmail}</p>
                        </div>
                        <span className="text-xs text-slate-500 whitespace-nowrap">{new Date(business.createdAt).toLocaleDateString()}</span>
                      </div>
                      <div className="mt-3 inline-flex items-center rounded-md bg-slate-800 px-2 py-1 text-xs font-mono text-slate-300 border border-slate-700">
                        Permit: {business.permitNumber}
                      </div>
                    </button>
                  ))
                )}
              </div>
            </section>
          </div>
        )}

        {/* --- MODALS --- */}
        
        {/* Report Details Modal */}
        <Modal
          isOpen={!!selectedReport && !confirmReportAction}
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
                    onClick={() => setConfirmReportAction({ id: selectedReport.id, status: "REJECTED" })}
                    disabled={actionLoading !== null}
                    className="flex-1 rounded-xl bg-red-500/20 px-4 py-3 text-sm font-semibold text-red-400 hover:bg-red-500/30 active:scale-95 transition-all border border-red-500/30 disabled:opacity-50"
                  >
                     Reject
                  </button>
                  <button
                    onClick={() => setConfirmReportAction({ id: selectedReport.id, status: "APPROVED" })}
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

        {/* Report Confirmation Modal */}
        <Modal
          isOpen={!!confirmReportAction}
          onClose={() => {
            setConfirmReportAction(null);
            setReportRejectionReason("");
          }}
          title="Confirm Action"
          maxWidth="max-w-md"
        >
          <div className="space-y-6">
            <p className="text-sm text-slate-300">
              Are you sure you want to <strong className={confirmReportAction?.status === "APPROVED" ? "text-emerald-400" : "text-red-400"}>{confirmReportAction?.status === "APPROVED" ? "approve" : "reject"}</strong> this report? This action cannot be undone.
            </p>
            {confirmReportAction?.status === "REJECTED" && (
              <div>
                <label htmlFor="dashboard-report-rejection-reason" className="mb-2 block text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Rejection reason
                </label>
                <textarea
                  id="dashboard-report-rejection-reason"
                  value={reportRejectionReason}
                  onChange={(event) => setReportRejectionReason(event.target.value)}
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
                  setConfirmReportAction(null);
                  setReportRejectionReason("");
                }}
                disabled={actionLoading !== null}
                className="flex-1 rounded-xl bg-slate-800 px-4 py-2.5 text-sm font-semibold text-slate-300 hover:bg-slate-700 active:scale-95 transition-all border border-slate-700 disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                onClick={() => confirmReportAction && handleReportStatusChange(confirmReportAction.id, confirmReportAction.status)}
                disabled={actionLoading !== null || (confirmReportAction?.status === "REJECTED" && !reportRejectionReason.trim())}
                className={`flex-1 rounded-xl px-4 py-2.5 text-sm font-semibold text-white shadow-lg active:scale-95 transition-all disabled:opacity-50 ${
                  confirmReportAction?.status === "APPROVED" 
                    ? "bg-emerald-600 hover:bg-emerald-500 shadow-emerald-900/20" 
                    : "bg-red-600 hover:bg-red-500 shadow-red-900/20"
                }`}
              >
                {actionLoading !== null ? "Processing..." : "Confirm"}
              </button>
            </div>
          </div>
        </Modal>

        {/* Business Details Modal */}
        <Modal
          isOpen={!!selectedBusiness && !confirmBusinessAction}
          onClose={() => setSelectedBusiness(null)}
          title="Business Details"
        >
          {selectedBusiness && (
            <div className="space-y-6">
              <div className="flex items-center gap-4">
                 <div className="h-16 w-16 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-xl font-bold text-slate-300 uppercase">
                    {selectedBusiness.businessName.charAt(0)}
                 </div>
                 <div>
                    <h3 className="text-2xl font-bold text-white flex items-center gap-2">
                      {selectedBusiness.businessName}
                      <span className="inline-flex items-center rounded-full bg-amber-500/10 px-2 py-0.5 text-xs font-medium text-amber-400 border border-amber-500/20">
                        Pending Verification
                      </span>
                    </h3>
                    <p className="text-sm text-slate-400">Registered on {new Date(selectedBusiness.createdAt).toLocaleDateString()}</p>
                 </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                 <div className="bg-slate-900/50 p-4 rounded-xl border border-slate-700/50">
                    <p className="text-xs text-slate-500 font-semibold uppercase tracking-wider mb-2">Owner Information</p>
                    <p className="text-sm font-medium text-slate-200">{selectedBusiness.ownerName || "Not provided"}</p>
                    <p className="text-sm text-slate-400">{selectedBusiness.ownerEmail}</p>
                 </div>
                 <div className="bg-slate-900/50 p-4 rounded-xl border border-slate-700/50">
                    <p className="text-xs text-slate-500 font-semibold uppercase tracking-wider mb-2">Business Credentials</p>
                    <p className="text-sm text-slate-300 font-mono tracking-wider">Permit: {selectedBusiness.permitNumber}</p>
                 </div>
              </div>

              <div className="bg-slate-900/50 p-4 rounded-xl border border-slate-700/50">
                 <p className="text-xs text-slate-500 font-semibold uppercase tracking-wider mb-2">Registered Address</p>
                 <p className="text-sm text-slate-300 leading-relaxed">{selectedBusiness.businessAddress}</p>
              </div>

              <div className="pt-4 mt-6 border-t border-slate-700/50 flex flex-col gap-3 sm:flex-row sm:justify-end">
                <button
                  onClick={() => setConfirmBusinessAction({ id: selectedBusiness.userId, action: "REJECT" })}
                  disabled={actionLoading !== null}
                  className="rounded-xl bg-red-500/20 px-6 py-2.5 text-sm font-semibold text-red-400 transition-all hover:bg-red-500/30 active:scale-95 disabled:opacity-50 border border-red-500/30"
                >
                  Reject Business
                </button>
                <button
                  onClick={() => setConfirmBusinessAction({ id: selectedBusiness.userId, action: "APPROVE" })}
                  disabled={actionLoading !== null}
                  className="rounded-xl bg-brand px-6 py-2.5 text-sm font-semibold text-slate-900 shadow-lg shadow-brand/20 transition-all hover:bg-brand/80 active:scale-95 disabled:opacity-50 flex items-center gap-2"
                >
                  Approve Business
                </button>
              </div>
            </div>
          )}
        </Modal>

        {/* Business Confirmation Modal */}
        <Modal
          isOpen={!!confirmBusinessAction}
          onClose={() => {
            setConfirmBusinessAction(null);
            setBusinessRejectionReason("");
          }}
          title={confirmBusinessAction?.action === "REJECT" ? "Confirm Rejection" : "Confirm Verification"}
          maxWidth="max-w-md"
        >
          <div className="space-y-6">
            <p className="text-sm text-slate-300">
              {confirmBusinessAction?.action === "REJECT"
                ? "Are you sure you want to reject this business account? This action cannot be undone."
                : "Are you sure you want to verify and grant platform access to this business?"}
            </p>
            {confirmBusinessAction?.action === "REJECT" && (
              <div>
                <label htmlFor="dashboard-business-rejection-reason" className="mb-2 block text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Rejection reason
                </label>
                <textarea
                  id="dashboard-business-rejection-reason"
                  value={businessRejectionReason}
                  onChange={(event) => setBusinessRejectionReason(event.target.value)}
                  disabled={actionLoading !== null}
                  rows={4}
                  className="w-full resize-none rounded-xl border border-slate-700 bg-slate-950/70 px-3 py-2 text-sm text-slate-200 outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/20 disabled:opacity-50"
                  placeholder="Explain why this business account is being rejected..."
                />
              </div>
            )}
            <div className="flex gap-3">
              <button
                onClick={() => {
                  setConfirmBusinessAction(null);
                  setBusinessRejectionReason("");
                }}
                disabled={actionLoading !== null}
                className="flex-1 rounded-xl bg-slate-800 px-4 py-2.5 text-sm font-semibold text-slate-300 hover:bg-slate-700 active:scale-95 transition-all border border-slate-700 disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  if (confirmBusinessAction?.action === "APPROVE") {
                    handleBusinessVerify(confirmBusinessAction.id);
                  } else if (confirmBusinessAction?.action === "REJECT") {
                    handleBusinessReject(confirmBusinessAction.id);
                  }
                }}
                disabled={actionLoading !== null || (confirmBusinessAction?.action === "REJECT" && !businessRejectionReason.trim())}
                className={`flex-1 rounded-xl px-4 py-2.5 text-sm font-semibold shadow-lg active:scale-95 transition-all disabled:opacity-50 ${
                  confirmBusinessAction?.action === "REJECT"
                    ? "bg-red-600 text-white hover:bg-red-500 shadow-red-900/20"
                    : "bg-brand text-slate-900 hover:bg-brand/80 shadow-brand/20"
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
