import { getJwt } from "@/shared/auth";

export type NotificationItem = {
  notifId: number;
  title: string;
  message: string;
  targetUrl?: string | null;
  read: boolean;
  createdAt: string;
};

type ApiErrorPayload = {
  message?: string;
  errors?: Record<string, string>;
};

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

export async function fetchNotifications(page = 0, size = 10): Promise<NotificationItem[]> {
  const response = await fetch(
    `/api/notifications?page=${encodeURIComponent(String(page))}&size=${encodeURIComponent(String(size))}`,
    {
      method: "GET",
      headers: {
        ...getAuthHeader(),
      },
    },
  );

  return handleResponse<NotificationItem[]>(response);
}

export async function fetchUnreadNotificationCount(): Promise<number> {
  const response = await fetch("/api/notifications/unread-count", {
    method: "GET",
    headers: {
      ...getAuthHeader(),
    },
  });

  const data = await handleResponse<{ count: number }>(response);
  return data.count;
}

export async function markAllNotificationsRead(): Promise<void> {
  const response = await fetch("/api/notifications/read-all", {
    method: "POST",
    headers: {
      ...getAuthHeader(),
    },
  });

  await handleResponse<{ success: boolean }>(response);
}

export async function markNotificationRead(notificationId: number): Promise<NotificationItem> {
  const response = await fetch(`/api/notifications/${notificationId}/read`, {
    method: "PATCH",
    headers: {
      ...getAuthHeader(),
    },
  });

  return handleResponse<NotificationItem>(response);
}

export function buildNotificationStreamUrl(): string | null {
  const token = getJwt();
  return token ? `/api/notifications/stream?token=${encodeURIComponent(token)}` : null;
}
