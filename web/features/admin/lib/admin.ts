import { getJwt } from "@/shared/auth";
import { Report } from "@/features/reports/lib/reports";

const API_BASE_URL = process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

export type AdminStats = {
  totalUsers: number;
  totalBusinesses: number;
  pendingReports: number;
  pendingBusinesses: number;
};

export type BusinessProfileAdmin = {
  userId: number;
  businessName: string;
  businessAddress: string;
  permitNumber: string;
  isVerified: boolean;
  isRejected?: boolean;
  rejectionReason?: string | null;
  ownerName: string;
  ownerEmail: string;
  createdAt: string;
};

export type ReportAdmin = {
  id: number;
  serialNumber: string;
  itemModel: string;
  description: string;
  status: "PENDING" | "APPROVED" | "REJECTED";
  rejectionReason?: string | null;
  createdAt: string;
  ownerName: string;
  ownerEmail: string;
  files: {
    id: number;
    fileUrl: string;
    fileType: string;
  }[];
};

async function fetchWithAuth(url: string, options: RequestInit = {}) {
  const token = getJwt();
  const headers = {
    ...options.headers,
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };

  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    if (response.status === 401 || response.status === 403) {
      throw new Error("Unauthorized access. Please ensure you are logged in as an administrator.");
    }
    throw new Error(`Request failed with status ${response.status}`);
  }

  return response.json();
}

export async function fetchAdminStats(): Promise<AdminStats> {
  return fetchWithAuth("/api/admin/stats");
}

export async function fetchPendingReports(): Promise<ReportAdmin[]> {
  return fetchWithAuth("/api/admin/reports/pending");
}

export async function updateReportStatus(
  id: number,
  status: "APPROVED" | "REJECTED" | "PENDING",
  rejectionReason?: string,
): Promise<ReportAdmin> {
  return fetchWithAuth(`/api/admin/reports/${id}/status`, {
    method: "PATCH",
    body: JSON.stringify({ status, rejectionReason }),
  });
}

export async function fetchPendingBusinesses(): Promise<BusinessProfileAdmin[]> {
  return fetchWithAuth("/api/admin/businesses/verify");
}

export async function fetchAllBusinesses(): Promise<BusinessProfileAdmin[]> {
  return fetchWithAuth("/api/admin/businesses");
}

export async function verifyBusiness(id: number): Promise<BusinessProfileAdmin> {
  return fetchWithAuth(`/api/admin/businesses/${id}/verify`, {
    method: "PATCH",
  });
}

export async function rejectBusiness(id: number, rejectionReason: string): Promise<BusinessProfileAdmin> {
  return fetchWithAuth(`/api/admin/businesses/${id}/reject`, {
    method: "PATCH",
    body: JSON.stringify({ rejectionReason }),
  });
}
