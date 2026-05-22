"use client";

import { useEffect, useState, useMemo } from "react";
import { useRouter } from "next/navigation";
import { getAuthRole, getJwt } from "@/shared/auth";
import { fetchAllBusinesses, BusinessProfileAdmin, verifyBusiness } from "@/features/admin/lib/admin";
import { Pagination } from "@/features/shared/components/pagination";
import { Modal } from "@/features/shared/components/modal";

const ITEMS_PER_PAGE = 8;
type TabType = "UNVERIFIED" | "VERIFIED";

export default function UsersPage() {
  const router = useRouter();
  const [businesses, setBusinesses] = useState<BusinessProfileAdmin[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<number | null>(null);

  // Tabs & Search
  const [activeTab, setActiveTab] = useState<TabType>("UNVERIFIED");
  const [searchQuery, setSearchQuery] = useState("");

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);

  // Modal
  const [selectedBusiness, setSelectedBusiness] = useState<BusinessProfileAdmin | null>(null);
  const [confirmVerifyId, setConfirmVerifyId] = useState<number | null>(null);

  useEffect(() => {
    const token = getJwt();
    const role = getAuthRole();

    if (!token || role !== "ADMIN") {
      router.replace("/login");
      return;
    }

    loadBusinesses();
  }, [router]);

  // Reset pagination when tab or search changes
  useEffect(() => {
    setCurrentPage(1);
  }, [activeTab, searchQuery]);

  async function loadBusinesses() {
    setIsLoading(true);
    try {
      const data = await fetchAllBusinesses();
      setBusinesses(data);
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  }

  async function handleVerify(id: number) {
    setActionLoading(id);
    try {
      await verifyBusiness(id);
      setBusinesses((prev) => 
        prev.map(b => b.userId === id ? { ...b, isVerified: true } : b)
      );
      setSelectedBusiness(null);
    } catch (error) {
      console.error(error);
      alert("Failed to verify business");
    } finally {
      setActionLoading(null);
    }
  }

  // Filter Data
  const filteredBusinesses = useMemo(() => {
    let filtered = businesses.filter(b => 
      activeTab === "VERIFIED" ? b.isVerified : !b.isVerified
    );

    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      filtered = filtered.filter(b => 
        b.businessName.toLowerCase().includes(q) ||
        b.permitNumber.toLowerCase().includes(q) ||
        (b.ownerEmail && b.ownerEmail.toLowerCase().includes(q))
      );
    }

    return filtered;
  }, [businesses, activeTab, searchQuery]);

  // Paginate Data
  const totalPages = Math.ceil(filteredBusinesses.length / ITEMS_PER_PAGE);
  const paginatedBusinesses = useMemo(() => {
    const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
    return filteredBusinesses.slice(startIndex, startIndex + ITEMS_PER_PAGE);
  }, [filteredBusinesses, currentPage]);

  const unverifiedCount = businesses.filter(b => !b.isVerified).length;
  const verifiedCount = businesses.filter(b => b.isVerified).length;

  return (
    <div className="min-h-screen text-slate-200">
      <main className="mx-auto w-full max-w-6xl px-4 pb-16 pt-32 sm:px-6 sm:pt-36 lg:px-8">
        
        <div className="mb-8">
          <h1 className="text-3xl font-extrabold text-white tracking-tight">Business Management</h1>
          <p className="mt-2 text-slate-400">Review and manage business accounts and their verification status.</p>
        </div>

        {/* Controls: Tabs & Search */}
        <div className="mb-6 flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between bg-[#0a1628]/40 p-2 rounded-2xl border border-slate-700/50 backdrop-blur-md">
          {/* Tabs */}
          <div className="flex bg-slate-900/50 p-1 rounded-xl w-full sm:w-auto">
            <button
              onClick={() => setActiveTab("UNVERIFIED")}
              className={`relative flex-1 sm:flex-none px-6 py-2.5 text-sm font-semibold rounded-lg transition-all ${
                activeTab === "UNVERIFIED"
                  ? "bg-brand text-slate-900 shadow-sm"
                  : "text-slate-400 hover:text-slate-200 hover:bg-slate-800/50"
              }`}
            >
              Pending
            </button>
            <button
              onClick={() => setActiveTab("VERIFIED")}
              className={`relative flex-1 sm:flex-none px-6 py-2.5 text-sm font-semibold rounded-lg transition-all ${
                activeTab === "VERIFIED"
                  ? "bg-brand text-slate-900 shadow-sm"
                  : "text-slate-400 hover:text-slate-200 hover:bg-slate-800/50"
              }`}
            >
              Verified
            </button>
          </div>

          {/* Search Bar */}
          <div className="relative w-full sm:w-80">
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
              <svg className="h-5 w-5 text-slate-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M9 3.5a5.5 5.5 0 100 11 5.5 5.5 0 000-11zM2 9a7 7 0 1112.452 4.391l3.328 3.329a.75.75 0 11-1.06 1.06l-3.329-3.328A7 7 0 012 9z" clipRule="evenodd" />
              </svg>
            </div>
            <input
              type="text"
              placeholder="Search by name, permit, email..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="block w-full rounded-xl border-0 bg-slate-900/50 py-2.5 pl-10 pr-4 text-slate-200 ring-1 ring-inset ring-slate-700/50 placeholder:text-slate-500 focus:ring-2 focus:ring-inset focus:ring-brand sm:text-sm sm:leading-6 transition-all"
            />
          </div>
        </div>

        {/* Content */}
        {isLoading ? (
          <div className="py-20 flex flex-col items-center justify-center">
            <div className="w-8 h-8 border-2 border-brand/30 border-t-brand rounded-full animate-spin mb-4" />
            <p className="text-sm text-slate-400">Loading businesses...</p>
          </div>
        ) : filteredBusinesses.length === 0 ? (
          <div className="py-24 text-center border border-dashed border-white/10 rounded-3xl bg-[#0a1628]/30 backdrop-blur-sm">
            <div className="mx-auto w-12 h-12 text-slate-600 mb-4 flex items-center justify-center">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" className="w-8 h-8 text-brand/50"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/></svg>
            </div>
            <h3 className="text-lg font-bold text-white mb-2">No businesses found</h3>
            <p className="text-sm text-slate-400">
              {searchQuery ? "Try adjusting your search query." : activeTab === "UNVERIFIED" ? "All registered businesses have been verified!" : "There are no verified businesses yet."}
            </p>
          </div>
        ) : (
          <div className="overflow-hidden rounded-3xl border border-slate-700/50 bg-[#0a1628]/40 shadow-lg backdrop-blur-xl">
            <table className="min-w-full divide-y divide-slate-700/50 text-left text-sm">
              <thead className="bg-slate-900/50">
                <tr>
                  <th scope="col" className="px-6 py-4 font-semibold text-slate-300">Business Name</th>
                  <th scope="col" className="px-6 py-4 font-semibold text-slate-300 hidden md:table-cell">Permit Number</th>
                  <th scope="col" className="px-6 py-4 font-semibold text-slate-300">Registration Date</th>
                  <th scope="col" className="px-6 py-4 font-semibold text-right text-slate-300"><span className="sr-only">Actions</span></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-700/50">
                {paginatedBusinesses.map((business) => (
                  <tr 
                    key={business.userId} 
                    className="hover:bg-slate-800/30 transition-colors cursor-pointer group"
                    onClick={() => setSelectedBusiness(business)}
                  >
                    <td className="px-6 py-4">
                      <div className="font-bold text-white group-hover:text-brand transition-colors flex items-center gap-2">
                        {business.businessName}
                        {business.isVerified && (
                          <svg className="w-4 h-4 text-emerald-400" fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd"/>
                          </svg>
                        )}
                      </div>
                      <div className="mt-1 text-xs text-slate-400 truncate max-w-[200px]">{business.ownerEmail}</div>
                    </td>
                    <td className="px-6 py-4 hidden md:table-cell">
                      <div className="text-sm font-mono text-slate-300">
                        {business.permitNumber}
                      </div>
                    </td>
                    <td className="px-6 py-4 text-slate-300 whitespace-nowrap">
                      {new Date(business.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-right">
                       <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setSelectedBusiness(business);
                        }}
                        className="inline-flex items-center justify-center rounded-lg bg-slate-800 border border-slate-700 px-3 py-1.5 text-xs font-semibold text-slate-300 transition-colors hover:bg-slate-700 hover:text-white"
                      >
                         View Details
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            
            <Pagination 
              currentPage={currentPage} 
              totalPages={totalPages} 
              onPageChange={setCurrentPage} 
            />
          </div>
        )}

        {/* Details Modal */}
        <Modal
          isOpen={!!selectedBusiness && !confirmVerifyId}
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
                      {selectedBusiness.isVerified && (
                        <span className="inline-flex items-center gap-1 rounded-full bg-emerald-500/10 px-2 py-0.5 text-xs font-medium text-emerald-400 border border-emerald-500/20">
                          <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd"/></svg>
                          Verified
                        </span>
                      )}
                      {!selectedBusiness.isVerified && (
                        <span className="inline-flex items-center rounded-full bg-amber-500/10 px-2 py-0.5 text-xs font-medium text-amber-400 border border-amber-500/20">
                          Pending Verification
                        </span>
                      )}
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

              {!selectedBusiness.isVerified && (
                <div className="pt-4 mt-6 border-t border-slate-700/50 flex justify-end">
                  <button
                    onClick={() => setConfirmVerifyId(selectedBusiness.userId)}
                    disabled={actionLoading !== null}
                    className="rounded-xl bg-brand px-6 py-2.5 text-sm font-semibold text-slate-900 shadow-lg shadow-brand/20 transition-all hover:bg-brand/80 active:scale-95 disabled:opacity-50 flex items-center gap-2"
                  >
                    Approve Business
                  </button>
                </div>
              )}
            </div>
          )}
        </Modal>

        {/* Confirmation Modal */}
        <Modal
          isOpen={!!confirmVerifyId}
          onClose={() => setConfirmVerifyId(null)}
          title="Confirm Verification"
          maxWidth="max-w-sm"
        >
          <div className="space-y-6">
            <p className="text-sm text-slate-300">
              Are you sure you want to verify and grant platform access to this business?
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setConfirmVerifyId(null)}
                disabled={actionLoading !== null}
                className="flex-1 rounded-xl bg-slate-800 px-4 py-2.5 text-sm font-semibold text-slate-300 hover:bg-slate-700 active:scale-95 transition-all border border-slate-700 disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  if (confirmVerifyId) {
                    handleVerify(confirmVerifyId);
                    setConfirmVerifyId(null);
                  }
                }}
                disabled={actionLoading !== null}
                className="flex-1 rounded-xl bg-brand px-4 py-2.5 text-sm font-semibold text-slate-900 shadow-lg shadow-brand/20 active:scale-95 transition-all hover:bg-brand/80 disabled:opacity-50"
              >
                {actionLoading !== null ? "Verifying..." : "Confirm"}
              </button>
            </div>
          </div>
        </Modal>

      </main>
    </div>
  );
}
