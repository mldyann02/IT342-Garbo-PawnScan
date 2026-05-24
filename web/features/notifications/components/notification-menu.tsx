"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import {
  NotificationItem,
  buildNotificationStreamUrl,
  clearNotifications,
  fetchNotifications,
  fetchUnreadNotificationCount,
  markAllNotificationsRead,
  markNotificationRead,
} from "@/features/notifications/lib/notifications";

function formatNotificationDate(value: string): string {
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleString();
}

function resolveNotificationTarget(notification: NotificationItem): string | null {
  if (notification.targetUrl) {
    return notification.targetUrl;
  }

  const title = notification.title.toLowerCase();
  const message = notification.message.toLowerCase();

  if (title.includes("stolen item match") || message.includes("matched")) {
    return "/reports?tab=matched";
  }

  if (title.includes("report") || message.includes("report")) {
    return "/reports";
  }

  if (title.includes("business") || message.includes("business account")) {
    return "/business";
  }

  return null;
}

type NotificationMenuProps = {
  isOpen: boolean;
};

export default function NotificationMenu({ isOpen }: NotificationMenuProps) {
  const router = useRouter();
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [isClearing, setIsClearing] = useState(false);

  useEffect(() => {
    let cancelled = false;

    async function loadNotifications() {
      try {
        const [items, count] = await Promise.all([
          fetchNotifications(0, 10),
          fetchUnreadNotificationCount(),
        ]);

        if (!cancelled) {
          setNotifications(items);
          setUnreadCount(count);
        }
      } catch {
        if (!cancelled) {
          setNotifications([]);
          setUnreadCount(0);
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }

    loadNotifications();

    const streamUrl = buildNotificationStreamUrl();
    if (!streamUrl) {
      return () => {
        cancelled = true;
      };
    }

    const stream = new EventSource(streamUrl);
    stream.addEventListener("notification", (event) => {
      const incoming = JSON.parse(event.data) as NotificationItem;
      setNotifications((current) => [
        incoming,
        ...current.filter((item) => item.notifId !== incoming.notifId),
      ].slice(0, 10));
      setUnreadCount((current) => current + 1);
    });
    stream.addEventListener("connected", (event) => {
      const data = JSON.parse(event.data) as { unreadCount?: number };
      if (typeof data.unreadCount === "number") {
        setUnreadCount(data.unreadCount);
      }
    });

    return () => {
      cancelled = true;
      stream.close();
    };
  }, []);

  const hasUnread = unreadCount > 0;
  const unreadLabel = useMemo(() => {
    if (unreadCount <= 0) {
      return "";
    }
    return unreadCount > 9 ? "9+" : String(unreadCount);
  }, [unreadCount]);

  async function handleMarkAllRead() {
    try {
      await markAllNotificationsRead();
      setUnreadCount(0);
      setNotifications((current) => current.map((item) => ({ ...item, read: true })));
    } catch {
      // The next refresh or stream event will recover the visible state.
    }
  }

  async function handleClearNotifications() {
    setIsClearing(true);
    try {
      await clearNotifications();
      setNotifications([]);
      setUnreadCount(0);
    } finally {
      setIsClearing(false);
    }
  }

  async function handleNotificationClick(notification: NotificationItem) {
    const targetUrl = resolveNotificationTarget(notification);

    if (!notification.read) {
      setNotifications((current) =>
        current.map((item) =>
          item.notifId === notification.notifId ? { ...item, read: true } : item,
        ),
      );
      setUnreadCount((current) => Math.max(current - 1, 0));

      markNotificationRead(notification.notifId).catch(() => {
        fetchUnreadNotificationCount().then(setUnreadCount).catch(() => {});
      });
    }

    if (targetUrl) {
      router.push(targetUrl);
    }
  }

  return (
    <>
      {hasUnread && (
        <span className="absolute right-12 top-1.5 inline-flex min-h-4 min-w-4 items-center justify-center rounded-full bg-status-stolen px-1 text-[10px] font-bold leading-none text-white">
          {unreadLabel}
        </span>
      )}

      {isOpen && (
      <div className="absolute right-0 top-11 w-[min(84vw,22rem)] overflow-hidden rounded-2xl bg-gradient-to-b from-slate-900/95 to-slate-950/95 shadow-2xl backdrop-blur-sm sm:right-12 sm:top-12 sm:w-80 border border-slate-700/40">
        <div className="flex items-center justify-between gap-3 px-4 py-4">
          <p className="text-sm font-semibold text-slate-100">Notifications</p>
          <div className="flex items-center gap-3">
            {notifications.length > 0 && (
              <button
                type="button"
                onClick={handleClearNotifications}
                disabled={isClearing}
                className="text-xs font-semibold text-slate-400 transition hover:text-slate-200 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {isClearing ? "Clearing..." : "Clear"}
              </button>
            )}
            {hasUnread && (
              <button
                type="button"
                onClick={handleMarkAllRead}
                className="text-xs font-semibold text-brand transition hover:text-brand/80"
              >
                Mark all read
              </button>
            )}
          </div>
        </div>

        {isLoading ? (
          <p className="border-t border-slate-700/30 px-4 py-4 text-sm text-slate-400">
            Loading notifications...
          </p>
        ) : notifications.length === 0 ? (
          <p className="border-t border-slate-700/30 px-4 py-4 text-sm text-slate-400">
            No notifications yet.
          </p>
        ) : (
          <ul className="max-h-96 divide-y divide-slate-700/30 overflow-y-auto">
            {notifications.map((notification) => (
              <li
                key={notification.notifId}
                className={`text-sm transition-colors duration-150 hover:bg-slate-800/40 ${
                  notification.read ? "text-slate-400" : "text-slate-200"
                }`}
              >
                <button
                  type="button"
                  onClick={() => handleNotificationClick(notification)}
                  className="flex w-full items-start gap-3 px-4 py-3 text-left"
                >
                  <span
                    className={`mt-1.5 h-2 w-2 flex-shrink-0 rounded-full ${
                      notification.read ? "bg-slate-600" : "bg-status-stolen"
                    }`}
                  />
                  <div className="min-w-0">
                    <p className="font-semibold text-slate-100">{notification.title}</p>
                    <p className="mt-1 leading-relaxed">{notification.message}</p>
                    <p className="mt-2 text-xs text-slate-500">
                      {formatNotificationDate(notification.createdAt)}
                    </p>
                  </div>
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
      )}
    </>
  );
}
