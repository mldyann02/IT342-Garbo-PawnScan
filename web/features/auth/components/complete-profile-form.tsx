"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { getAuthRole, getJwt, storeJwt } from "@/shared/auth";
import { RegistrationRole } from "@/features/auth/lib/validation";

type CompleteProfileValues = {
  phoneNumber: string;
  businessName: string;
  businessAddress: string;
  permitNumber: string;
};

type ApiResponse = {
  message?: string;
  code?: string;
  token?: string;
  jwt?: string;
  accessToken?: string;
  access_token?: string;
  errors?: Record<string, string>;
};

const initialValues: CompleteProfileValues = {
  phoneNumber: "+639",
  businessName: "",
  businessAddress: "",
  permitNumber: "",
};

export default function CompleteProfileForm() {
  const router = useRouter();
  const [role, setRole] = useState<RegistrationRole | null>(null);
  const [values, setValues] = useState<CompleteProfileValues>(initialValues);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiMessage, setApiMessage] = useState<{ type: "error" | "success"; text: string } | null>(null);

  useEffect(() => {
    const storedRole = getAuthRole();
    if (storedRole === "BUSINESS" || storedRole === "USER") {
      setRole(storedRole as RegistrationRole);
    } else {
      setRole("USER");
    }
  }, []);

  if (!role) {
    return null;
  }

  const isBusiness = role === "BUSINESS";

  function updateField<K extends keyof CompleteProfileValues>(key: K, value: CompleteProfileValues[K]) {
    if (key === "phoneNumber") {
      let v = String(value || "");
      if (!v.startsWith("+639")) {
        v = "+639";
      }
      const digits = v.slice(4).replace(/\D/g, "");
      v = "+639" + digits.slice(0, 9);
      setValues((current) => ({ ...current, [key]: v }));
      return;
    }
    setValues((current) => ({ ...current, [key]: value }));
  }

  function normalizePhilippinePhone(input: string): string {
    const v = (input || "").trim();
    if (!v) return v;
    if (/^\+639\d{9}$/.test(v)) return v;
    const digits = v.replace(/[^\d]/g, "");
    if (digits.length === 11 && digits.startsWith("09")) return "+63" + digits.slice(1);
    if (digits.length === 12 && digits.startsWith("63")) return "+" + digits;
    return v;
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setApiMessage(null);

    if (!values.phoneNumber.trim() || values.phoneNumber.trim().length < 10) {
      setApiMessage({ type: "error", text: "Please provide a valid contact number." });
      return;
    }

    if (isBusiness) {
      if (!values.businessName.trim() || !values.businessAddress.trim() || !values.permitNumber.trim()) {
        setApiMessage({ type: "error", text: "Please fill in all business details." });
        return;
      }
    }

    setIsSubmitting(true);

    const normalizedPhone = normalizePhilippinePhone(values.phoneNumber.trim());

    if (normalizedPhone !== values.phoneNumber.trim()) {
      updateField("phoneNumber", normalizedPhone);
    }

    const payload = {
      phoneNumber: normalizedPhone,
      businessName: isBusiness ? values.businessName.trim() : undefined,
      businessAddress: isBusiness ? values.businessAddress.trim() : undefined,
      permitNumber: isBusiness ? values.permitNumber.trim() : undefined,
    };

    try {
      const response = await fetch("/api/auth/complete-profile", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          ...(getJwt() ? { Authorization: `Bearer ${getJwt()}` } : {}),
        },
        body: JSON.stringify(payload),
      });

      const data = (await response.json().catch(() => ({}))) as ApiResponse;

      if (!response.ok) {
        const fieldErrorMessages = data.errors ? Object.values(data.errors).join(" ") : "";
        setApiMessage({
          type: "error",
          text: data.message || fieldErrorMessages || "Failed to save profile. Please try again.",
        });
        return;
      }

      setApiMessage({
        type: "success",
        text: data.message || "Profile completed successfully. Redirecting...",
      });

      const token = data.token || data.jwt || data.accessToken || data.access_token;
      if (token) {
        storeJwt(token);
      }

      setTimeout(() => {
        router.push(isBusiness ? "/business" : "/dashboard");
      }, 500);
    } catch {
      setApiMessage({
        type: "error",
        text: "Could not reach the server. Please check your connection.",
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="min-h-screen w-full px-4 py-6 sm:px-6 lg:px-8 flex items-center justify-center">
      <section className="w-full max-w-lg p-8 sm:p-10 rounded-3xl shadow-[0_24px_60px_rgba(0,0,0,0.4)] bg-slate-900/60 border border-slate-700/40 backdrop-blur-2xl relative overflow-hidden">
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-3/4 h-[2px] bg-gradient-to-r from-transparent via-brand/80 to-transparent" />
        <h2 className="text-2xl font-semibold text-slate-100">
          Almost there!
        </h2>
        <p className="mt-2 text-sm text-slate-300">
          Please provide a few more details to complete your {isBusiness ? "Business" : "User"} account setup.
        </p>

        <form className="mt-6 space-y-4" onSubmit={handleSubmit} noValidate>
          <div>
            <label htmlFor="phoneNumber" className="mb-1 block text-sm font-medium text-slate-200">
              Contact Number
            </label>
            <input
              required
              id="phoneNumber"
              type="tel"
              value={values.phoneNumber}
              onChange={(e) => updateField("phoneNumber", e.target.value)}
              onPaste={(e) => {
                const text = (e.clipboardData || (window as any).clipboardData).getData("text") || "";
                let v = text.replace(/[^\d+]/g, "");
                if (v.startsWith("09")) v = "+63" + v.slice(1);
                else if (v.startsWith("639")) v = "+" + v;
                else if (!v.startsWith("+639")) v = "+639";
                const digits = v.slice(4).replace(/\D/g, "");
                e.preventDefault();
                updateField("phoneNumber", "+639" + digits.slice(0, 9));
              }}
              className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
              placeholder="e.g. +639171234567"
            />
          </div>

          {isBusiness && (
            <>
              <div>
                <label htmlFor="businessName" className="mb-1 block text-sm font-medium text-slate-200">
                  Business Name
                </label>
                <input
                  required
                  id="businessName"
                  type="text"
                  value={values.businessName}
                  onChange={(e) => updateField("businessName", e.target.value)}
                  className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                />
              </div>
              <div>
                <label htmlFor="businessAddress" className="mb-1 block text-sm font-medium text-slate-200">
                  Business Address
                </label>
                <input
                  required
                  id="businessAddress"
                  type="text"
                  value={values.businessAddress}
                  onChange={(e) => updateField("businessAddress", e.target.value)}
                  className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                />
              </div>
              <div>
                <label htmlFor="permitNumber" className="mb-1 block text-sm font-medium text-slate-200">
                  Permit Number
                </label>
                <input
                  required
                  id="permitNumber"
                  type="text"
                  value={values.permitNumber}
                  onChange={(e) => updateField("permitNumber", e.target.value)}
                  className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                />
              </div>
            </>
          )}

          {apiMessage && (
            <div
              className={`rounded-[10px] border px-3 py-2 text-sm ${
                apiMessage.type === "error"
                  ? "border-status-stolen/50 bg-status-stolen/10 text-status-stolen"
                  : "border-status-clean/40 bg-status-clean/10 text-status-clean"
              }`}
            >
              {apiMessage.text}
            </div>
          )}

          <button
            type="submit"
            disabled={isSubmitting}
            className="min-h-12 w-full rounded-[10px] bg-brand px-4 py-3 text-base font-semibold text-slate-950 transition hover:brightness-90 active:scale-95 active:brightness-85 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/40 disabled:opacity-50"
          >
            {isSubmitting ? "Saving..." : "Complete Profile"}
          </button>
        </form>
      </section>
    </main>
  );
}
