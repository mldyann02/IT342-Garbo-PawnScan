"use client";

import React from "react";
import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { storeAuthRole, storeAuthUser, storeJwt } from "@/shared/auth";

type ApiResponse = {
  message?: string;
  token?: string;
  jwt?: string;
  accessToken?: string;
  access_token?: string;
  email?: string;
  role?: string;
  registrationStatus?: string;
  user?: {
    email?: string;
    role?: string;
  };
  errors?: Record<string, string>;
};

type GoogleCredentialResponse = {
  credential?: string;
};

type GoogleConfigResponse = {
  configured?: boolean;
  clientId?: string | null;
};

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (options: {
            client_id: string;
            callback: (response: GoogleCredentialResponse) => void;
            ux_mode?: "popup" | "redirect";
          }) => void;
          renderButton: (
            parent: HTMLElement,
            options: {
              theme?: "outline" | "filled_blue" | "filled_black";
              size?: "large" | "medium" | "small";
              width?: number;
              text?: "signin_with" | "signup_with" | "continue_with" | "signin";
              shape?: "rectangular" | "pill" | "circle" | "square";
            },
          ) => void;
        };
      };
    };
  }
}

const GOOGLE_SCRIPT_ID = "google-identity-services";

type GoogleAuthButtonProps = {
  mode: "login" | "register";
  role?: string;
};

function GoogleIcon() {
  return (
    <svg className="h-5 w-5" aria-hidden="true" viewBox="0 0 24 24">
      <path
        d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
        fill="#4285F4"
      />
      <path
        d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
        fill="#34A853"
      />
      <path
        d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
        fill="#FBBC05"
      />
      <path
        d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
        fill="#EA4335"
      />
    </svg>
  );
}

export default function GoogleAuthButton({ mode, role: selectedRole }: GoogleAuthButtonProps) {
  const router = useRouter();
  const buttonRef = useRef<HTMLDivElement | null>(null);
  const [message, setMessage] = useState<{ type: "error" | "success"; text: string } | null>(null);
  const [isReady, setIsReady] = useState(false);
  const roleRef = useRef(selectedRole);

  useEffect(() => {
    roleRef.current = selectedRole;
  }, [selectedRole]);

  useEffect(() => {
    let isMounted = true;

    async function loadGoogleSignIn() {
      const clientId = await resolveGoogleClientId();
      if (!isMounted) {
        return;
      }

      if (!clientId) {
        setMessage({
          type: "error",
          text: "Google sign-in needs a Google OAuth client ID. Set GOOGLE_CLIENT_ID and restart the app.",
        });
        return;
      }

      const initializeGoogle = () => {
        if (!window.google || !buttonRef.current) {
          return;
        }

        buttonRef.current.innerHTML = "";
        window.google.accounts.id.initialize({
          client_id: clientId,
          callback: handleCredentialResponse,
          ux_mode: "popup",
        });
        window.google.accounts.id.renderButton(buttonRef.current, {
          theme: "outline",
          size: "large",
          width: buttonRef.current.offsetWidth || 320,
          text: "continue_with",
          shape: "rectangular",
        });
        setIsReady(true);
      };

      const existingScript = document.getElementById(GOOGLE_SCRIPT_ID);
      if (existingScript) {
        initializeGoogle();
        return;
      }

      const script = document.createElement("script");
      script.id = GOOGLE_SCRIPT_ID;
      script.src = "https://accounts.google.com/gsi/client";
      script.async = true;
      script.defer = true;
      script.onload = initializeGoogle;
      script.onerror = () =>
        setMessage({
          type: "error",
          text: "Google sign-in could not be loaded.",
        });
      document.head.appendChild(script);
    }

    loadGoogleSignIn();

    return () => {
      isMounted = false;
    };
  }, []);

  async function resolveGoogleClientId(): Promise<string | null> {
    const publicClientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
    if (publicClientId?.trim()) {
      return publicClientId.trim();
    }

    try {
      const response = await fetch("/api/auth/google/config", {
        cache: "no-store",
      });
      const data = (await response.json().catch(() => ({}))) as GoogleConfigResponse;
      const clientId = typeof data.clientId === "string" ? data.clientId.trim() : "";
      return data.configured && clientId ? clientId : null;
    } catch {
      return null;
    }
  }

  async function handleCredentialResponse(response: GoogleCredentialResponse) {
    setMessage(null);

    if (!response.credential) {
      setMessage({
        type: "error",
        text: "Google did not return an ID token.",
      });
      return;
    }

    try {
      const authResponse = await fetch("/api/auth/google", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          token: response.credential,
          role: roleRef.current,
        }),
      });

      const data = (await authResponse.json().catch(() => ({}))) as ApiResponse;

      if (!authResponse.ok) {
        const fieldErrorMessages = data.errors
          ? Object.values(data.errors).join(" ")
          : "";
        setMessage({
          type: "error",
          text:
            data.message ||
            fieldErrorMessages ||
            "Google authentication failed. Please try again.",
        });
        return;
      }

      const token = data.token || data.jwt || data.accessToken || data.access_token;
      if (token) {
        storeJwt(token);
      }

      const role = data.role || data.user?.role || "";
      if (role) {
        storeAuthRole(role);
      }

      const email = data.email || data.user?.email || "";
      if (email) {
        storeAuthUser(email);
      }

      setMessage({
        type: "success",
        text: data.message || "Google authentication successful. Redirecting...",
      });

      const registrationStatus = data.registrationStatus;
      let redirectUrl;
      
      if (registrationStatus === 'INCOMPLETE') {
        redirectUrl = "/complete-profile";
      } else {
        redirectUrl = role === "ADMIN" ? "/admin/dashboard" : role === "BUSINESS" ? "/business" : "/dashboard";
      }
      
      router.push(redirectUrl);
    } catch {
      setMessage({
        type: "error",
        text: "Google authentication failed: We could not reach the server.",
      });
    }
  }

  return (
    <div className="space-y-3">
      <div className="relative min-h-12 w-full">
        <div
          aria-hidden="true"
          className={`flex min-h-12 w-full items-center justify-center gap-3 rounded-[10px] border border-slate-700/50 bg-slate-800/50 px-3 py-2 text-sm font-semibold text-slate-100 transition ${
            isReady
              ? "hover:bg-slate-700/50 active:scale-95"
              : "opacity-80"
          }`}
        >
          <GoogleIcon />
          {isReady ? "Google" : "Loading Google..."}
        </div>
        <div
          ref={buttonRef}
          aria-label={mode === "register" ? "Continue with Google registration" : "Continue with Google login"}
          className={`absolute inset-0 z-10 min-h-12 w-full overflow-hidden opacity-0 ${
            isReady ? "" : "pointer-events-none"
          } [&>div]:!h-12 [&>div]:!w-full [&_iframe]:!h-12 [&_iframe]:!w-full`}
        />
      </div>
      {!isReady && !message && (
        <span className="sr-only" aria-live="polite">
          Loading Google...
        </span>
      )}
      {message && (
        <div
          role="status"
          aria-live="polite"
          className={`rounded-[10px] border px-3 py-2 text-sm ${
            message.type === "error"
              ? "border-status-stolen/50 bg-status-stolen/10 text-status-stolen"
              : "border-status-clean/40 bg-status-clean/10 text-status-clean"
          }`}
        >
          {message.text}
        </div>
      )}
    </div>
  );
}
