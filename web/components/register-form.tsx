"use client";

import Link from "next/link";
import React from "react";
import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { storeJwt } from "@/lib/auth";
import {
  RegistrationFormValues,
  RegistrationRole,
  hasValidationErrors,
  validateRegistrationForm,
} from "@/lib/validation";

type ApiResponse = {
  message?: string;
  code?: string;
  token?: string;
  jwt?: string;
  accessToken?: string;
  access_token?: string;
  email?: string;
  role?: string;
  errors?: Record<string, string>;
};

const initialValues: RegistrationFormValues = {
  email: "",
  password: "",
  confirmPassword: "",
  fullName: "",
  contactNumber: "",
  businessName: "",
  businessAddress: "",
  permitNumber: "",
  role: "INDIVIDUAL",
};

function EyeIcon({ open }: { open: boolean }) {
  if (open) {
    return (
      <svg
        aria-hidden="true"
        viewBox="0 0 24 24"
        className="h-5 w-5"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
      >
        <path d="M3 3l18 18" />
        <path d="M10.58 10.58A2 2 0 0012 14a2 2 0 001.42-.58" />
        <path d="M9.88 5.09A9.77 9.77 0 0112 5c5.2 0 9.27 3.11 10.58 7a10.74 10.74 0 01-4.27 5.36" />
        <path d="M6.61 6.61A10.75 10.75 0 001.42 12a10.74 10.74 0 003.26 4.62" />
      </svg>
    );
  }

  return (
    <svg
      aria-hidden="true"
      viewBox="0 0 24 24"
      className="h-5 w-5"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
    >
      <path d="M1.42 12C2.73 8.11 6.8 5 12 5s9.27 3.11 10.58 7c-1.31 3.89-5.38 7-10.58 7S2.73 15.89 1.42 12z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

export default function RegisterForm() {
  const router = useRouter();
  const [values, setValues] = useState<RegistrationFormValues>(initialValues);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [isConfirmPasswordVisible, setIsConfirmPasswordVisible] =
    useState(false);
  const [submitAttempted, setSubmitAttempted] = useState(false);
  const [apiMessage, setApiMessage] = useState<{
    type: "error" | "success";
    text: string;
  } | null>(null);

  const errors = useMemo(() => validateRegistrationForm(values), [values]);
  const canSubmit = !hasValidationErrors(errors) && !isSubmitting;
  const isBusiness = values.role === "BUSINESS";

  function updateField<K extends keyof RegistrationFormValues>(
    key: K,
    value: RegistrationFormValues[K],
  ) {
    if (key === "contactNumber") {
      let v = String(value || "");
      // Allow only digits and a single leading +
      v = v.replace(/[^\d+]/g, "");
      if ((v.match(/\+/g) || []).length > 1) {
        v = v.replace(/\+/g, (m, i) => (i === v.indexOf("+") ? "+" : ""));
      }

      // If there's a +, treat as international format (+63##########) -> max 13 chars
      if (v.startsWith("+")) {
        v = v.slice(0, 13);
      } else if (v.startsWith("0")) {
        // Local format 0########## -> max 11 chars
        v = v.slice(0, 11);
      } else {
        // Otherwise cap at 13 to prevent overly long input (covers pasted +63...)
        v = v.slice(0, 13);
      }

      setValues((current) => ({
        ...current,
        [key]: v as RegistrationFormValues[typeof key],
      }));
      return;
    }

    setValues((current) => ({ ...current, [key]: value }));
  }

  function normalizePhilippinePhone(input: string): string {
    const v = (input || "").trim();
    if (!v) return v;

    // If already in +63XXXXXXXXXX format and valid length, return as-is
    if (/^\+639\d{9}$/.test(v)) return v;

    // If local 0XXXXXXXXXX convert to +63XXXXXXXXXX
    if (/^0\d{10}$/.test(v)) return "+63" + v.slice(1);

    // If starts with 63XXXXXXXXXX (missing +), add +
    if (/^63\d{10}$/.test(v)) return "+" + v;

    // If user pasted or entered +63 but with extra chars, try to extract digits
    const digits = v.replace(/[^\d]/g, "");
    if (digits.length === 11 && digits.startsWith("09")) {
      return "+63" + digits.slice(1);
    }
    if (digits.length === 12 && digits.startsWith("63")) {
      return "+" + digits;
    }

    // fallback to trimmed original
    return v;
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitAttempted(true);
    setApiMessage(null);

    if (hasValidationErrors(errors)) {
      return;
    }

    setIsSubmitting(true);

    const normalizedPhone = normalizePhilippinePhone(
      values.contactNumber.trim(),
    );

    // update visible value to normalized form so user sees the stored format
    if (normalizedPhone !== values.contactNumber.trim()) {
      updateField("contactNumber", normalizedPhone);
    }

    const payload = {
      fullName: isBusiness
        ? values.businessName.trim()
        : values.fullName.trim(),
      email: values.email.trim(),
      password: values.password,
      phoneNumber: normalizedPhone,
      role: values.role,
      business_name: isBusiness ? values.businessName.trim() : undefined,
      business_address: isBusiness ? values.businessAddress.trim() : undefined,
      permit_number: isBusiness ? values.permitNumber.trim() : undefined,
    };

    try {
      const response = await fetch("/api/auth/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      const data = (await response.json().catch(() => ({}))) as ApiResponse;

      if (!response.ok) {
        const fieldErrorMessages = data.errors
          ? Object.values(data.errors).join(" ")
          : "";
        const mainMessage =
          data.message ||
          fieldErrorMessages ||
          "Please check your details and try again.";
        setApiMessage({
          type: "error",
          text: `Registration failed: ${mainMessage}`,
        });
        return;
      }

      const token =
        data.token || data.jwt || data.accessToken || data.access_token;
      if (token) {
        storeJwt(token);
      }

      const registeredEmail = typeof data.email === "string" ? data.email : "";
      const registeredRole = typeof data.role === "string" ? data.role : "";
      const params = new URLSearchParams({
        registered: "1",
      });

      if (registeredEmail) {
        params.set("email", registeredEmail);
      }

      if (registeredRole) {
        params.set("role", registeredRole);
      }

      setApiMessage({
        type: "success",
        text:
          data.message || "Registration successful. Redirecting to login...",
      });

      setTimeout(() => {
        router.push(`/login?${params.toString()}`);
      }, 500);
    } catch {
      setApiMessage({
        type: "error",
        text: "Registration failed: We could not reach the server. Please try again.",
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="min-h-screen w-full px-4 py-6 sm:px-6 lg:px-8 flex items-center justify-center">
      <section className="mx-auto grid min-h-[calc(100vh-3rem)] w-full max-w-6xl grid-cols-1 overflow-hidden md:grid-cols-1 lg:grid-cols-2">
        <aside className="relative flex flex-col justify-center p-6 sm:p-8 lg:p-12 lg:sticky lg:top-24 lg:self-start">
          <div
            className="absolute inset-0 bg-gradient-to-br from-brand/15 via-transparent to-transparent"
            aria-hidden="true"
          />
          <div className="relative">
            <p className="mb-2 text-sm font-medium uppercase tracking-[0.12em] text-brand">
              PawnScan
            </p>
            <h1 className="text-3xl font-semibold leading-tight text-slate-100 sm:text-4xl">
              Secure Your Transactions with PawnScan
            </h1>
            <p className="mt-4 max-w-xl text-base leading-relaxed text-slate-300">
              PawnScan empowers individuals and businesses to verify high-value
              items instantly, preventing the circulation of stolen goods.
            </p>
            <div className="mt-6 inline-flex min-h-12 items-center gap-3 rounded-[10px] border border-border-muted bg-slate-950/70 px-4 py-3 text-slate-200">
              <svg
                aria-hidden="true"
                viewBox="0 0 24 24"
                className="h-6 w-6 text-status-clean"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M12 3l8 4v6c0 5-3.4 8.6-8 10-4.6-1.4-8-5-8-10V7l8-4z" />
                <path d="M9 12l2 2 4-4" />
              </svg>
              <p className="text-sm">
                Trusted verification for safer transactions.
              </p>
            </div>
          </div>
        </aside>

        <section className="flex items-start p-4 sm:p-8 lg:p-12">
          <div className="w-full glass-panel p-6 sm:p-8 rounded-md form-fixed thin-scrollbar">
            <h2 className="text-2xl font-semibold text-slate-100">
              Create Your Account
            </h2>
            <p className="mt-2 text-sm text-slate-300">
              Join the PawnScan network in a few simple steps
            </p>

            <form
              className="mt-6 space-y-4"
              onSubmit={handleSubmit}
              noValidate
              aria-label="Registration form"
            >
              <fieldset>
                <legend className="mb-2 block text-sm font-medium text-slate-200">
                  Account Type
                </legend>
                <div className="relative grid min-h-12 grid-cols-2 rounded-[10px] p-1">
                  <span
                    aria-hidden="true"
                    className={`absolute bottom-1 top-1 z-0 w-[calc(50%-4px)] rounded-[8px] shadow-sm transition-transform duration-300 ${
                      values.role === "BUSINESS" ? "bg-brand" : "bg-brand"
                    } ${
                      values.role === "BUSINESS"
                        ? "translate-x-[calc(100%+4px)]"
                        : "translate-x-0"
                    }`}
                    style={{ opacity: 0.95 }}
                  />
                  <button
                    type="button"
                    aria-label="Select Individual account"
                    aria-pressed={values.role === "INDIVIDUAL"}
                    onClick={() => updateField("role", "INDIVIDUAL")}
                    className={`relative z-10 flex min-h-12 items-center justify-center rounded-[8px] px-4 text-sm font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand ${
                      values.role === "INDIVIDUAL"
                        ? "text-slate-900"
                        : "text-slate-300 hover:text-slate-100"
                    }`}
                  >
                    Individual
                  </button>
                  <button
                    type="button"
                    aria-label="Select Business account"
                    aria-pressed={values.role === "BUSINESS"}
                    onClick={() => updateField("role", "BUSINESS")}
                    className={`relative z-10 flex min-h-12 items-center justify-center rounded-[8px] px-4 text-sm font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand ${
                      values.role === "BUSINESS"
                        ? "text-slate-900"
                        : "text-slate-300 hover:text-slate-100"
                    }`}
                  >
                    Business
                  </button>
                </div>
              </fieldset>

              {!isBusiness && (
                <div>
                  <label
                    htmlFor="fullName"
                    className="mb-1 block text-sm font-medium text-slate-200"
                  >
                    Full Name
                  </label>
                  <input
                    id="fullName"
                    aria-label="Full Name"
                    type="text"
                    value={values.fullName}
                    onChange={(event) =>
                      updateField("fullName", event.target.value)
                    }
                    className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                    autoComplete="name"
                  />
                  {(submitAttempted || values.fullName.length > 0) &&
                    errors.fullName && (
                      <p className="mt-1 text-sm text-status-stolen">
                        {errors.fullName}
                      </p>
                    )}
                </div>
              )}

              {isBusiness && (
                <>
                  <div>
                    <label
                      htmlFor="businessName"
                      className="mb-1 block text-sm font-medium text-slate-200"
                    >
                      Business Name
                    </label>
                    <input
                      id="businessName"
                      aria-label="Business Name"
                      type="text"
                      value={values.businessName}
                      onChange={(event) =>
                        updateField("businessName", event.target.value)
                      }
                      className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                      autoComplete="organization"
                    />
                    {(submitAttempted || values.businessName.length > 0) &&
                      errors.businessName && (
                        <p className="mt-1 text-sm text-status-stolen">
                          {errors.businessName}
                        </p>
                      )}
                  </div>

                  <div>
                    <label
                      htmlFor="businessAddress"
                      className="mb-1 block text-sm font-medium text-slate-200"
                    >
                      Business Address
                    </label>
                    <input
                      id="businessAddress"
                      aria-label="Business Address"
                      type="text"
                      value={values.businessAddress}
                      onChange={(event) =>
                        updateField("businessAddress", event.target.value)
                      }
                      className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                      autoComplete="street-address"
                    />
                    {(submitAttempted || values.businessAddress.length > 0) &&
                      errors.businessAddress && (
                        <p className="mt-1 text-sm text-status-stolen">
                          {errors.businessAddress}
                        </p>
                      )}
                  </div>

                  <div>
                    <label
                      htmlFor="permitNumber"
                      className="mb-1 block text-sm font-medium text-slate-200"
                    >
                      Permit Number
                    </label>
                    <input
                      id="permitNumber"
                      aria-label="Permit Number"
                      type="text"
                      value={values.permitNumber}
                      onChange={(event) =>
                        updateField("permitNumber", event.target.value)
                      }
                      className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                    />
                    {(submitAttempted || values.permitNumber.length > 0) &&
                      errors.permitNumber && (
                        <p className="mt-1 text-sm text-status-stolen">
                          {errors.permitNumber}
                        </p>
                      )}
                  </div>
                </>
              )}

              <div>
                <label
                  htmlFor="email"
                  className="mb-1 block text-sm font-medium text-slate-200"
                >
                  Email Address
                </label>
                <input
                  id="email"
                  aria-label="Email Address"
                  type="email"
                  value={values.email}
                  onChange={(event) => updateField("email", event.target.value)}
                  className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                  autoComplete="email"
                />
                {(submitAttempted || values.email.length > 0) &&
                  errors.email && (
                    <p className="mt-1 text-sm text-status-stolen">
                      {errors.email}
                    </p>
                  )}
              </div>

              <div>
                <label
                  htmlFor="contactNumber"
                  className="mb-1 block text-sm font-medium text-slate-200"
                >
                  Contact Number
                </label>
                <input
                  id="contactNumber"
                  aria-label="Contact Number"
                  type="tel"
                  value={values.contactNumber}
                  onChange={(event) =>
                    updateField("contactNumber", event.target.value)
                  }
                  onPaste={(e) => {
                    const text =
                      (
                        e.clipboardData || (window as any).clipboardData
                      ).getData("text") || "";
                    // sanitize pasted value same as typing
                    let v = text.replace(/[^\d+]/g, "");
                    if (!v) {
                      e.preventDefault();
                      return;
                    }
                    if (v.startsWith("+")) v = v.slice(0, 13);
                    else if (v.startsWith("0")) v = v.slice(0, 11);
                    else v = v.slice(0, 13);
                    e.preventDefault();
                    updateField("contactNumber", v);
                  }}
                  className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                  autoComplete="tel"
                  placeholder="e.g. +639171234567"
                />
                {(submitAttempted || values.contactNumber.length > 0) &&
                  errors.contactNumber && (
                    <p className="mt-1 text-sm text-status-stolen">
                      {errors.contactNumber}
                    </p>
                  )}
              </div>

              <div>
                <label
                  htmlFor="password"
                  className="mb-1 block text-sm font-medium text-slate-200"
                >
                  Password
                </label>
                <div className="flex gap-2">
                  <input
                    id="password"
                    aria-label="Password"
                    type={isPasswordVisible ? "text" : "password"}
                    value={values.password}
                    onChange={(event) =>
                      updateField("password", event.target.value)
                    }
                    className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                    autoComplete="new-password"
                  />
                  <button
                    type="button"
                    aria-label={
                      isPasswordVisible ? "Hide password" : "Show password"
                    }
                    onClick={() => setIsPasswordVisible((current) => !current)}
                    className="inline-flex min-h-12 min-w-12 items-center justify-center rounded-[10px] border border-border-muted bg-slate-900 px-3 text-slate-200 transition hover:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                  >
                    <EyeIcon open={isPasswordVisible} />
                  </button>
                </div>
                {(submitAttempted || values.password.length > 0) &&
                  errors.password && (
                    <p className="mt-1 text-sm text-status-stolen">
                      {errors.password}
                    </p>
                  )}
              </div>

              <div>
                <label
                  htmlFor="confirmPassword"
                  className="mb-1 block text-sm font-medium text-slate-200"
                >
                  Confirm Password
                </label>
                <div className="flex gap-2">
                  <input
                    id="confirmPassword"
                    aria-label="Confirm Password"
                    type={isConfirmPasswordVisible ? "text" : "password"}
                    value={values.confirmPassword}
                    onChange={(event) =>
                      updateField("confirmPassword", event.target.value)
                    }
                    className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                    autoComplete="new-password"
                  />
                  <button
                    type="button"
                    aria-label={
                      isConfirmPasswordVisible
                        ? "Hide confirm password"
                        : "Show confirm password"
                    }
                    onClick={() =>
                      setIsConfirmPasswordVisible((current) => !current)
                    }
                    className="inline-flex min-h-12 min-w-12 items-center justify-center rounded-[10px] border border-border-muted bg-slate-900 px-3 text-slate-200 transition hover:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                  >
                    <EyeIcon open={isConfirmPasswordVisible} />
                  </button>
                </div>
                {(submitAttempted || values.confirmPassword.length > 0) &&
                  errors.confirmPassword && (
                    <p className="mt-1 text-sm text-status-stolen">
                      {errors.confirmPassword}
                    </p>
                  )}
              </div>

              {apiMessage && (
                <div
                  role="status"
                  aria-live="polite"
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
                aria-label="Register Account"
                disabled={!canSubmit}
                className="min-h-12 w-full rounded-[10px] bg-brand px-4 py-3 text-base font-semibold text-slate-950 transition hover:brightness-95 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/40 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {isSubmitting ? "Registering..." : "Register Account"}
              </button>
            </form>

            <p className="mt-4 text-sm text-slate-300 text-center">
              Already registered?{" "}
              <Link
                href="/login"
                className="font-medium text-brand transition hover:underline"
              >
                Sign in here.
              </Link>
            </p>
          </div>
        </section>
      </section>
    </main>
  );
}
