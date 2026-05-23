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
};

export default function GoogleAuthButton({ mode }: GoogleAuthButtonProps) {
  const router = useRouter();
  const buttonRef = useRef<HTMLDivElement | null>(null);
  const [message, setMessage] = useState<{ type: "error" | "success"; text: string } | null>(null);
  const [isReady, setIsReady] = useState(false);

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

      const redirectUrl =
        role === "ADMIN" ? "/admin/dashboard" : role === "BUSINESS" ? "/business" : "/dashboard";
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
      <div
        ref={buttonRef}
        aria-label={mode === "register" ? "Continue with Google registration" : "Continue with Google login"}
        className="min-h-12 w-full [&>div]:mx-auto"
      />
      {!isReady && !message && (
        <div className="flex min-h-12 w-full items-center justify-center rounded-[10px] border border-slate-700/50 bg-slate-800/50 px-3 py-2 text-sm font-semibold text-slate-100">
          Loading Google...
        </div>
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
