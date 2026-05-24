"use client";

import Link from "next/link";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import BusinessDashboardHeader from "@/features/business/components/business-dashboard-header";
import UserDashboardHeader from "@/features/dashboard/components/user-dashboard-header";
import {
  fetchProfile,
  updateProfile,
  UserProfile,
  UserProfileUpdate,
} from "@/features/profile/lib/profile";
import { getAuthRole, getAuthUser, getJwt } from "@/shared/auth";

type FormState = {
  fullName: string;
  email: string;
  phoneNumber: string;
  businessName: string;
  businessAddress: string;
  permitNumber: string;
};

const EMPTY_FORM: FormState = {
  fullName: "",
  email: "",
  phoneNumber: "",
  businessName: "",
  businessAddress: "",
  permitNumber: "",
};

function getBusinessVerified(profile: UserProfile) {
  return Boolean(
    profile.businessProfile?.verified ?? profile.businessProfile?.isVerified,
  );
}

function toFormState(profile: UserProfile): FormState {
  return {
    fullName: profile.fullName || "",
    email: profile.email || "",
    phoneNumber: profile.phoneNumber || "",
    businessName: profile.businessProfile?.businessName || "",
    businessAddress: profile.businessProfile?.businessAddress || "",
    permitNumber: profile.businessProfile?.permitNumber || "",
  };
}

function formatDate(value?: string) {
  if (!value) {
    return "Not available";
  }

  return new Date(value).toLocaleDateString(undefined, {
    month: "long",
    day: "numeric",
    year: "numeric",
  });
}

export default function ProfilePage() {
  const router = useRouter();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const storedRole = useMemo(() => {
    if (typeof window === "undefined") {
      return "";
    }

    return getAuthRole() || "";
  }, []);

  useEffect(() => {
    const token = getJwt();
    const authenticatedEmail = getAuthUser();

    if (!token && !authenticatedEmail) {
      router.replace("/login");
      return;
    }

    fetchProfile()
      .then((data) => {
        setProfile(data);
        setForm(toFormState(data));
      })
      .catch((err) => {
        setError(
          err instanceof Error
            ? err.message
            : "Unable to load your profile right now.",
        );
      })
      .finally(() => setIsLoading(false));
  }, [router]);

  const role = profile?.role || storedRole;
  const isBusiness = role === "BUSINESS";
  const Header = isBusiness ? BusinessDashboardHeader : UserDashboardHeader;
  const initials =
    form.fullName
      .split(" ")
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase())
      .join("") || "PS";

  function updateField(field: keyof FormState, value: string) {
    setForm((current) => ({ ...current, [field]: value }));
    setError("");
    setSuccess("");
  }

  function resetForm() {
    if (profile) {
      setForm(toFormState(profile));
    }
    setIsEditing(false);
    setError("");
    setSuccess("");
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (!form.fullName.trim()) {
      setError("Full name is required.");
      return;
    }

    if (isBusiness) {
      if (
        !form.businessName.trim() ||
        !form.businessAddress.trim() ||
        !form.permitNumber.trim()
      ) {
        setError("Business name, address, and permit number are required.");
        return;
      }
    }

    const payload: UserProfileUpdate = {
      fullName: form.fullName.trim(),
      phoneNumber: form.phoneNumber.trim(),
    };

    if (isBusiness) {
      payload.businessName = form.businessName.trim();
      payload.businessAddress = form.businessAddress.trim();
      payload.permitNumber = form.permitNumber.trim();
    }

    setIsSaving(true);
    try {
      const updated = await updateProfile(payload);
      setProfile(updated);
      setForm(toFormState(updated));
      setIsEditing(false);
      setSuccess("Profile updated successfully.");
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Unable to save your profile.",
      );
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <div className="min-h-screen text-slate-200">
      <Header />
      <main className="mx-auto w-full max-w-5xl px-4 pb-16 pt-32 sm:px-6 sm:pt-36 lg:px-8">
        <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-brand">
              Account Settings
            </p>
            <h1 className="mt-2 text-3xl font-extrabold tracking-tight text-white sm:text-4xl">
              My Profile
            </h1>
          </div>
          <Link
            href={isBusiness ? "/business" : "/dashboard"}
            className="inline-flex items-center justify-center rounded-xl border border-slate-700/60 bg-slate-900/50 px-4 py-2.5 text-sm font-semibold text-slate-200 transition-all hover:border-slate-600 hover:bg-slate-800/80"
          >
            Back to Dashboard
          </Link>
        </div>

        {isLoading ? (
          <div className="flex min-h-[28rem] flex-col items-center justify-center rounded-2xl border border-white/5 bg-[#0a1628]/70">
            <div className="mb-4 h-8 w-8 animate-spin rounded-full border-2 border-brand/30 border-t-brand" />
            <p className="text-sm text-slate-400">Loading your profile...</p>
          </div>
        ) : profile ? (
          <div className="grid gap-6 lg:grid-cols-[0.8fr_1.2fr]">
            <aside className="overflow-hidden rounded-2xl border border-white/5 bg-[#0a1628]/80 shadow-[0_24px_60px_rgba(0,0,0,0.28)] backdrop-blur-xl">
              <div className="border-b border-white/5 p-6">
                <div className="flex items-center gap-4">
                  <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-2xl border border-brand/25 bg-brand/15 text-xl font-black text-brand">
                    {initials}
                  </div>
                  <div className="min-w-0">
                    <h2 className="truncate text-xl font-bold text-white">
                      {form.fullName || "PawnScan User"}
                    </h2>
                    <p className="mt-1 truncate text-sm text-slate-400">
                      {form.email}
                    </p>
                  </div>
                </div>
              </div>

              <dl className="space-y-4 p-6">
                <div>
                  <dt className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                    Account Type
                  </dt>
                  <dd className="mt-1 inline-flex rounded-full border border-brand/20 bg-brand/10 px-3 py-1 text-xs font-bold uppercase tracking-wider text-brand">
                    {isBusiness ? "Business" : "User"}
                  </dd>
                </div>
                <div>
                  <dt className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                    Member Since
                  </dt>
                  <dd className="mt-1 text-sm font-medium text-slate-200">
                    {formatDate(profile.createdAt)}
                  </dd>
                </div>
                {isBusiness && (
                  <div>
                    <dt className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                      Verification Status
                    </dt>
                    <dd
                      className={`mt-1 inline-flex rounded-full border px-3 py-1 text-xs font-bold uppercase tracking-wider ${
                        getBusinessVerified(profile)
                          ? "border-brand/25 bg-brand/10 text-brand"
                          : "border-amber-400/25 bg-amber-400/10 text-amber-300"
                      }`}
                    >
                      {getBusinessVerified(profile) ? "Verified" : "Pending"}
                    </dd>
                  </div>
                )}
              </dl>
            </aside>

            <section className="rounded-2xl border border-white/5 bg-[#0a1628]/80 shadow-[0_24px_60px_rgba(0,0,0,0.28)] backdrop-blur-xl">
              <div className="flex flex-col gap-4 border-b border-white/5 p-6 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <h2 className="text-xl font-bold text-white">
                    Profile Details
                  </h2>
                  <p className="mt-1 text-sm text-slate-400">
                    Keep your contact and business information current.
                  </p>
                </div>
                {!isEditing && (
                  <button
                    type="button"
                    onClick={() => setIsEditing(true)}
                    className="inline-flex items-center justify-center rounded-xl bg-brand px-5 py-2.5 text-sm font-bold text-slate-950 shadow-lg shadow-brand/15 transition-all hover:brightness-110 active:scale-95"
                  >
                    Edit Profile
                  </button>
                )}
              </div>

              <form onSubmit={handleSubmit} className="space-y-6 p-6">
                {error && (
                  <div className="rounded-xl border border-red-500/25 bg-red-500/10 px-4 py-3 text-sm text-red-200">
                    {error}
                  </div>
                )}
                {success && (
                  <div className="rounded-xl border border-brand/25 bg-brand/10 px-4 py-3 text-sm text-brand">
                    {success}
                  </div>
                )}

                <div className="grid gap-5 sm:grid-cols-2">
                  <label className="space-y-2">
                    <span className="text-sm font-semibold text-slate-200">
                      Full Name
                    </span>
                    <input
                      value={form.fullName}
                      onChange={(event) =>
                        updateField("fullName", event.target.value)
                      }
                      disabled={!isEditing || isSaving}
                      className="w-full rounded-xl border border-slate-700/70 bg-slate-950/40 px-4 py-3 text-sm text-white outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/20 disabled:cursor-not-allowed disabled:text-slate-400"
                    />
                  </label>

                  <label className="space-y-2">
                    <span className="text-sm font-semibold text-slate-200">
                      Email Address
                    </span>
                    <input
                      value={form.email}
                      disabled
                      className="w-full cursor-not-allowed rounded-xl border border-slate-700/70 bg-slate-950/60 px-4 py-3 text-sm text-slate-400 outline-none"
                    />
                  </label>

                  <label className="space-y-2 sm:col-span-2">
                    <span className="text-sm font-semibold text-slate-200">
                      Phone Number
                    </span>
                    <input
                      value={form.phoneNumber}
                      onChange={(event) =>
                        updateField("phoneNumber", event.target.value)
                      }
                      disabled={!isEditing || isSaving}
                      placeholder="+639171234567"
                      className="w-full rounded-xl border border-slate-700/70 bg-slate-950/40 px-4 py-3 text-sm text-white outline-none transition placeholder:text-slate-600 focus:border-brand focus:ring-2 focus:ring-brand/20 disabled:cursor-not-allowed disabled:text-slate-400"
                    />
                  </label>
                </div>

                {isBusiness && (
                  <div className="space-y-5 border-t border-white/5 pt-6">
                    <h3 className="text-base font-bold text-white">
                      Business Information
                    </h3>
                    <div className="grid gap-5 sm:grid-cols-2">
                      <label className="space-y-2">
                        <span className="text-sm font-semibold text-slate-200">
                          Business Name
                        </span>
                        <input
                          value={form.businessName}
                          onChange={(event) =>
                            updateField("businessName", event.target.value)
                          }
                          disabled={!isEditing || isSaving}
                          className="w-full rounded-xl border border-slate-700/70 bg-slate-950/40 px-4 py-3 text-sm text-white outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/20 disabled:cursor-not-allowed disabled:text-slate-400"
                        />
                      </label>

                      <label className="space-y-2">
                        <span className="text-sm font-semibold text-slate-200">
                          Permit Number
                        </span>
                        <input
                          value={form.permitNumber}
                          onChange={(event) =>
                            updateField("permitNumber", event.target.value)
                          }
                          disabled={true}
                          className="w-full rounded-xl border border-slate-700/70 bg-slate-950/40 px-4 py-3 text-sm text-white outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/20 disabled:cursor-not-allowed disabled:text-slate-400"
                        />
                      </label>

                      <label className="space-y-2 sm:col-span-2">
                        <span className="text-sm font-semibold text-slate-200">
                          Business Address
                        </span>
                        <textarea
                          value={form.businessAddress}
                          onChange={(event) =>
                            updateField("businessAddress", event.target.value)
                          }
                          disabled={!isEditing || isSaving}
                          rows={4}
                          className="w-full resize-none rounded-xl border border-slate-700/70 bg-slate-950/40 px-4 py-3 text-sm text-white outline-none transition focus:border-brand focus:ring-2 focus:ring-brand/20 disabled:cursor-not-allowed disabled:text-slate-400"
                        />
                      </label>
                    </div>
                  </div>
                )}

                {isEditing && (
                  <div className="flex flex-col-reverse gap-3 border-t border-white/5 pt-6 sm:flex-row sm:justify-end">
                    <button
                      type="button"
                      onClick={resetForm}
                      disabled={isSaving}
                      className="inline-flex items-center justify-center rounded-xl border border-slate-700/70 bg-slate-900/50 px-5 py-2.5 text-sm font-semibold text-slate-200 transition-all hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      disabled={isSaving}
                      className="inline-flex items-center justify-center rounded-xl bg-brand px-5 py-2.5 text-sm font-bold text-slate-950 shadow-lg shadow-brand/15 transition-all hover:brightness-110 active:scale-95 disabled:cursor-not-allowed disabled:opacity-70"
                    >
                      {isSaving ? "Saving..." : "Save Changes"}
                    </button>
                  </div>
                )}
              </form>
            </section>
          </div>
        ) : (
          <div className="rounded-2xl border border-red-500/25 bg-red-500/10 p-6 text-sm text-red-200">
            {error || "Unable to load your profile."}
          </div>
        )}
      </main>
    </div>
  );
}
