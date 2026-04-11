import { getJwt } from "@/lib/auth";

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
  createdAt: string;
  updatedAt?: string;
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

export async function fetchReports(): Promise<Report[]> {
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

  return handleResponse<Report>(response);
}

export async function updateReport(reportId: number, payload: ReportPayload): Promise<Report> {
  const response = await fetchWithTimeout(`/api/reports/${reportId}`, {
    method: "PUT",
    headers: {
      ...getAuthHeader(),
    },
    body: buildFormData(payload),
  });

  return handleResponse<Report>(response);
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
}