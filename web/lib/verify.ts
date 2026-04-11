import { getJwt } from "@/lib/auth";

export type VerificationResult = "CLEAN" | "STOLEN";

export type StolenReportSummary = {
  reportId: number;
  serialNumber: string;
  itemModel: string;
  description: string;
  dateReported: string;
};

export type VerifySearchResponse = {
  status: VerificationResult;
  serial: string;
  report?: StolenReportSummary | null;
};

export type SearchLog = {
  searchedSerial: string;
  result: VerificationResult;
  timestamp: string;
  matchedReportId: number | null;
};

export type StolenMatch = {
  searchedSerial: string;
  timestamp: string;
  matchedReportId: number | null;
  itemModel?: string | null;
  description?: string | null;
  dateReported?: string | null;
  victimName?: string | null;
  victimEmail?: string | null;
  victimPhoneNumber?: string | null;
  evidenceFileUrl?: string | null;
  evidenceFileType?: "IMAGE" | "PDF" | null;
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

async function handleResponse<T>(response: Response): Promise<T> {
  const data = (await response.json().catch(() => ({}))) as T & ApiErrorPayload;

  if (!response.ok) {
    const fieldErrors = data.errors ? Object.values(data.errors).join(" ") : "";
    throw new Error(data.message || fieldErrors || "Request failed");
  }

  return data;
}

export async function verifySerialNumber(serial: string): Promise<VerifySearchResponse> {
  const response = await fetchWithTimeout(
    `/api/verify/search?serial=${encodeURIComponent(serial)}`,
    {
      method: "GET",
      headers: {
        ...getAuthHeader(),
      },
    },
  );

  return handleResponse<VerifySearchResponse>(response);
}

export async function createSearchLog(serial: string): Promise<SearchLog> {
  const response = await fetchWithTimeout("/api/verify/log", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeader(),
    },
    body: JSON.stringify({ serial: serial.trim() }),
  });

  return handleResponse<SearchLog>(response);
}

export async function fetchSearchHistory(page = 0, size = 20): Promise<SearchLog[]> {
  const response = await fetchWithTimeout(
    `/api/verify/history?page=${encodeURIComponent(String(page))}&size=${encodeURIComponent(String(size))}`,
    {
      method: "GET",
      headers: {
        ...getAuthHeader(),
      },
    },
  );

  return handleResponse<SearchLog[]>(response);
}

export async function fetchStolenMatches(page = 0, size = 20): Promise<StolenMatch[]> {
  const response = await fetchWithTimeout(
    `/api/verify/matches?page=${encodeURIComponent(String(page))}&size=${encodeURIComponent(String(size))}`,
    {
      method: "GET",
      headers: {
        ...getAuthHeader(),
      },
    },
  );

  return handleResponse<StolenMatch[]>(response);
}
