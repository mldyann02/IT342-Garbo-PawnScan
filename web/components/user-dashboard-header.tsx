"use client";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import { clearAuthSession } from "@/lib/auth";

const NAV_ITEMS = [
  { href: "/dashboard", label: "Home" },
  { href: "/reports/create", label: "Report Item" },
  { href: "/reports", label: "My Reports" },
];

type DropdownType = "notifications" | "profile" | null;

export default function UserDashboardHeader() {
  const router = useRouter();
  const pathname = usePathname();
  const [openDropdown, setOpenDropdown] = useState<DropdownType>(null);
  const dropdownContainerRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (
        dropdownContainerRef.current &&
        !dropdownContainerRef.current.contains(event.target as Node)
      ) {
        setOpenDropdown(null);
      }
    }

    function handleEsc(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setOpenDropdown(null);
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    document.addEventListener("keydown", handleEsc);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleEsc);
    };
  }, []);

  function toggleDropdown(type: Exclude<DropdownType, null>) {
    setOpenDropdown((current) => (current === type ? null : type));
  }

  function handleLogout() {
    clearAuthSession();
    router.push("/login");
  }

  return (
    <nav className="fixed left-1/2 top-3 z-50 w-[95%] max-w-6xl -translate-x-1/2 rounded-2xl bg-gradient-to-r from-slate-900/60 via-slate-900/50 to-slate-800/55 backdrop-blur-xl shadow-[0_18px_38px_rgba(0,0,0,0.35)] sm:top-5 md:rounded-full">
      <div className="flex items-center justify-between px-3 py-3 sm:px-6 lg:px-8">
        <Link
          href="/dashboard"
          className="text-brand font-bold text-lg tracking-wide transition-all duration-200 ease-out hover:text-brand/85 active:scale-[0.98] sm:text-xl"
        >
          PawnScan
        </Link>

        <div className="hidden md:flex items-center gap-8">
          {NAV_ITEMS.map((item) => {
            const isActive = pathname === item.href;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`relative py-1 text-base font-medium transition-all duration-200 ease-out active:scale-[0.98] ${
                  isActive
                    ? "text-slate-100"
                    : "text-slate-300/80 hover:text-slate-200"
                }`}
              >
                {item.label}
                <span
                  className={`absolute -bottom-1 left-0 h-[2px] rounded-full bg-brand transition-all duration-200 ${
                    isActive ? "w-full opacity-100" : "w-0 opacity-0"
                  }`}
                />
              </Link>
            );
          })}
        </div>

        <div className="relative flex items-center gap-1.5 sm:gap-2" ref={dropdownContainerRef}>
          <button
            type="button"
            onClick={() => toggleDropdown("notifications")}
            className="relative inline-flex h-9 w-9 items-center justify-center rounded-full border border-border-muted/70 bg-slate-900/45 text-slate-300 transition-all duration-200 ease-out hover:bg-slate-800/75 hover:text-slate-100 active:scale-[0.96] sm:h-10 sm:w-10"
            aria-label="Open notifications"
            aria-expanded={openDropdown === "notifications"}
            aria-haspopup="menu"
          >
            <svg
              className="h-5 w-5"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
              aria-hidden="true"
            >
              <path d="M14.857 17.082A23.848 23.848 0 0018 16.5v-5.25a6 6 0 10-12 0v5.25c1.135.24 2.194.434 3.143.582m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0" />
            </svg>
            <span className="absolute right-2 top-2 h-2 w-2 rounded-full bg-status-stolen" />
          </button>

          {openDropdown === "notifications" && (
            <div className="absolute right-0 top-11 w-[min(84vw,20rem)] overflow-hidden rounded-xl border border-border-muted/70 bg-slate-950 shadow-2xl sm:right-12 sm:top-12 sm:w-72">
              <div className="border-b border-border-muted/70 px-4 py-3">
                <p className="text-sm font-semibold text-slate-100">Notifications</p>
              </div>
              <ul className="divide-y divide-border-muted/50">
                <li className="px-4 py-3 text-sm text-slate-300">
                  Your report for bicycle frame #PS-1102 is under review.
                </li>
                <li className="px-4 py-3 text-sm text-slate-300">
                  A nearby partner business viewed one of your report details.
                </li>
                <li className="px-4 py-3 text-sm text-slate-300">
                  No critical alerts right now. You are all caught up.
                </li>
              </ul>
            </div>
          )}

          <button
            type="button"
            onClick={() => toggleDropdown("profile")}
            className="inline-flex h-9 w-9 items-center justify-center rounded-full border border-border-muted/70 bg-slate-900/45 text-slate-300 transition-all duration-200 ease-out hover:bg-slate-800/75 hover:text-slate-100 active:scale-[0.96] sm:h-10 sm:w-10"
            aria-label="Open profile menu"
            aria-expanded={openDropdown === "profile"}
            aria-haspopup="menu"
          >
            <svg
              className="h-5 w-5"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
              aria-hidden="true"
            >
              <path d="M15.75 6.75a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0z" />
              <path d="M4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
            </svg>
          </button>

          {openDropdown === "profile" && (
            <div className="absolute right-0 top-11 w-44 overflow-hidden rounded-xl border border-border-muted/70 bg-slate-950 shadow-2xl sm:top-12 sm:w-48">
              <button
                type="button"
                onClick={() => {
                  setOpenDropdown(null);
                  router.push("/dashboard");
                }}
                className="w-full px-4 py-3 text-left text-sm text-slate-300 transition-all duration-200 ease-out hover:bg-slate-800/80 hover:text-slate-100 active:scale-[0.99]"
              >
                My Profile
              </button>
              <button
                type="button"
                onClick={handleLogout}
                className="w-full border-t border-border-muted/60 px-4 py-3 text-left text-sm text-red-300 transition-all duration-200 ease-out hover:bg-red-500/15 active:scale-[0.99]"
              >
                Logout
              </button>
            </div>
          )}
        </div>
      </div>

      <div className="flex md:hidden items-center justify-center gap-6 px-3 pb-2 pt-0.5">
        {NAV_ITEMS.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`relative whitespace-nowrap py-1 text-[0.95rem] font-medium transition-all duration-200 ease-out active:scale-[0.98] ${
                isActive
                  ? "text-slate-100"
                  : "text-slate-300/80 hover:text-slate-200"
              }`}
            >
              {item.label}
              <span
                className={`absolute -bottom-1 left-0 h-[2px] rounded-full bg-brand transition-all duration-200 ${
                  isActive ? "w-full opacity-100" : "w-0 opacity-0"
                }`}
              />
            </Link>
          );
        })}
      </div>
    </nav>
  );
}