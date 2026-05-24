import { getJwt } from "@/shared/auth";

export type ReportFileType = "IMAGE" | "PDF";

export type ReportFile = {
  id: number;
  fileUrl: string;
  fileType: ReportFileType;
};

export type Report = {
  id: number;
  serialNumber: string;
  itemModel: string;
  description: string;
  status?: "PENDING" | "APPROVED" | "REJECTED";
  rejectionReason?: string | null;
  createdAt: string;
  updatedAt?: string;
  files: ReportFile[];
};

export type MatchedReport = {
  matchId: number;
  reportId: number;
  serialNumber: string;
  itemModel: string;
  description: string;
  status?: "PENDING" | "APPROVED" | "REJECTED";
  reportCreatedAt: string;
  matchedAt: string;
  matchedByBusinessName?: string | null;
  matchedByBusinessEmail?: string | null;
  files: ReportFile[];
};

export type ReportPayload = {
  serialNumber: string;
  itemModel: string;
  description: string;
  file?: File | null;
};

type ApiErrorPayload = {
  message?: string;
  errors?: Record<string, string>;
};

const REQUEST_TIMEOUT_MS = 12000;

async function fetchWithTimeout(
  input: RequestInfo | URL,
  init: RequestInit,
  timeoutMs = REQUEST_TIMEOUT_MS,
): Promise<Response> {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutMs);

  try {
    return await fetch(input, {
      ...init,
      signal: controller.signal,
    });
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError") {
      throw new Error("Request timed out. Please try again.");
    }
    throw error;
  } finally {
    clearTimeout(timeout);
  }
}

function getAuthHeader(): HeadersInit {
  const token = getJwt();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

function buildFormData(payload: ReportPayload): FormData {
  const formData = new FormData();
  formData.append("serialNumber", payload.serialNumber.trim());
  formData.append("itemModel", payload.itemModel.trim());
  formData.append("description", payload.description.trim());

  if (payload.file) {
    formData.append("file", payload.file);
  }

  return formData;
}

async function handleResponse<T>(response: Response): Promise<T> {
  const data = (await response.json().catch(() => ({}))) as T & ApiErrorPayload;

  if (!response.ok) {
    const fieldErrors = data.errors ? Object.values(data.errors).join(" ") : "";
    throw new Error(data.message || fieldErrors || "Request failed");
  }

  return data;
}

let reportsCache: Report[] | null = null;
let reportsCachePromise: Promise<Report[]> | null = null;

export function getCachedReports(): Report[] | null {
  return reportsCache;
}

export function invalidateReportsCache(): void {
  reportsCache = null;
  reportsCachePromise = null;
}

export async function fetchReports(force = false): Promise<Report[]> {
  if (!force && reportsCache) {
    // Refresh in background to keep data fresh
    fetchReportsFromApi().then(data => { reportsCache = data; }).catch(console.error);
    return reportsCache;
  }
  if (!force && reportsCachePromise) {
    return reportsCachePromise;
  }
  
  reportsCachePromise = fetchReportsFromApi();
  try {
    const data = await reportsCachePromise;
    reportsCache = data;
    return data;
  } catch (error) {
    reportsCachePromise = null;
    throw error;
  }
}

export async function fetchMatchedReports(page = 0, size = 20): Promise<MatchedReport[]> {
  const response = await fetchWithTimeout(
    `/api/reports/matched?page=${encodeURIComponent(String(page))}&size=${encodeURIComponent(String(size))}`,
    {
      method: "GET",
      headers: {
        ...getAuthHeader(),
      },
    },
  );

  return handleResponse<MatchedReport[]>(response);
}

async function fetchReportsFromApi(): Promise<Report[]> {
  const response = await fetchWithTimeout("/api/reports", {
    method: "GET",
    headers: {
      ...getAuthHeader(),
    },
  });

  return handleResponse<Report[]>(response);
}

export async function createReport(payload: ReportPayload): Promise<Report> {
  const response = await fetchWithTimeout("/api/reports", {
    method: "POST",
    headers: {
      ...getAuthHeader(),
    },
    body: buildFormData(payload),
  });

  const data = await handleResponse<Report>(response);
  invalidateReportsCache();
  return data;
}

export async function updateReport(reportId: number, payload: ReportPayload): Promise<Report> {
  const response = await fetchWithTimeout(`/api/reports/${reportId}`, {
    method: "PUT",
    headers: {
      ...getAuthHeader(),
    },
    body: buildFormData(payload),
  });

  const data = await handleResponse<Report>(response);
  invalidateReportsCache();
  return data;
}

export async function deleteReport(reportId: number): Promise<void> {
  const response = await fetchWithTimeout(`/api/reports/${reportId}`, {
    method: "DELETE",
    headers: {
      ...getAuthHeader(),
    },
  });

  if (!response.ok) {
    await handleResponse<{ message?: string }>(response);
  }
  invalidateReportsCache();
}

