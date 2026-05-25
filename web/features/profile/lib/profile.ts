import { getJwt } from "@/shared/auth";

export type BusinessProfileDetails = {
  userId: number;
  businessName: string;
  businessAddress: string;
  permitNumber: string;
  verified?: boolean;
  isVerified?: boolean;
  rejected?: boolean;
  isRejected?: boolean;
  rejectionReason?: string | null;
  createdAt?: string;
  updatedAt?: string;
};

export type UserProfile = {
  userId: number;
  email: string;
  fullName: string;
  phoneNumber?: string | null;
  role: "USER" | "BUSINESS" | "ADMIN" | string;
  createdAt?: string;
  businessProfile?: BusinessProfileDetails | null;
  message?: string;
};

export type UserProfileUpdate = {
  fullName: string;
  phoneNumber: string;
  businessName?: string;
  businessAddress?: string;
  permitNumber?: string;
};

type ApiErrorPayload = {
  message?: string;
  errors?: Record<string, string>;
};

const BACKEND_BASE_URL =
  process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

function getAuthHeaders(): HeadersInit {
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

export async function fetchProfile(): Promise<UserProfile> {
  const response = await fetch(`${BACKEND_BASE_URL}/api/auth/profile`, {
    method: "GET",
    headers: {
      ...getAuthHeaders(),
    },
  });

  return handleResponse<UserProfile>(response);
}

export async function updateProfile(
  profile: UserProfileUpdate,
): Promise<UserProfile> {
  const response = await fetch(`${BACKEND_BASE_URL}/api/auth/profile`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeaders(),
    },
    body: JSON.stringify(profile),
  });

  return handleResponse<UserProfile>(response);
}
