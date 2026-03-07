'use client';

import Link from 'next/link';
import React from 'react';
import { useEffect, useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { storeAuthUser, storeJwt, storeAuthRole } from '@/lib/auth';
import { validateEmail, validatePassword } from '@/lib/validation';

type LoginValues = {
  email: string;
  password: string;
};

type ApiResponse = {
  message?: string;
  token?: string;
  jwt?: string;
  accessToken?: string;
  access_token?: string;
  code?: string;
  errors?: Record<string, string>;
};

function EyeIcon({ open }: { open: boolean }) {
  if (open) {
    return (
      <svg aria-hidden="true" viewBox="0 0 24 24" className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="2">
        <path d="M3 3l18 18" />
        <path d="M10.58 10.58A2 2 0 0012 14a2 2 0 001.42-.58" />
        <path d="M9.88 5.09A9.77 9.77 0 0112 5c5.2 0 9.27 3.11 10.58 7a10.74 10.74 0 01-4.27 5.36" />
        <path d="M6.61 6.61A10.75 10.75 0 001.42 12a10.74 10.74 0 003.26 4.62" />
      </svg>
    );
  }

  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M1.42 12C2.73 8.11 6.8 5 12 5s9.27 3.11 10.58 7c-1.31 3.89-5.38 7-10.58 7S2.73 15.89 1.42 12z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

export default function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [values, setValues] = useState<LoginValues>({ email: '', password: '' });
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitAttempted, setSubmitAttempted] = useState(false);
  const [apiMessage, setApiMessage] = useState<{ type: 'error' | 'success'; text: string } | null>(null);

  useEffect(() => {
    const isRegistered = searchParams.get('registered') === '1';
    if (!isRegistered) {
      return;
    }

    const registeredEmail = searchParams.get('email');
    const registeredRole = searchParams.get('role');
    const roleLabel = registeredRole === 'BUSINESS' ? 'Business account' : 'User account';

    setApiMessage({
      type: 'success',
      text: registeredEmail
        ? `${roleLabel} for ${registeredEmail} registered successfully. Please log in.`
        : 'Registration successful. Please log in.'
    });
  }, [searchParams]);

  const errors = useMemo(() => {
    const nextErrors: Partial<Record<keyof LoginValues, string>> = {};

    const emailError = validateEmail(values.email);
    const passwordError = validatePassword(values.password);

    if (emailError) {
      nextErrors.email = emailError;
    }

    if (passwordError) {
      nextErrors.password = passwordError;
    }

    return nextErrors;
  }, [values]);

  const canSubmit = !Object.values(errors).some(Boolean) && !isSubmitting;

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitAttempted(true);
    setApiMessage(null);

    if (Object.values(errors).some(Boolean)) {
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          email: values.email.trim(),
          password: values.password
        })
      });

      const data = (await response.json().catch(() => ({}))) as ApiResponse;

      if (!response.ok) {
        const fieldErrorMessages = data.errors ? Object.values(data.errors).join(' ') : '';
        const mainMessage = data.message || fieldErrorMessages || 'Please check your credentials and try again.';
        setApiMessage({
          type: 'error',
          text: `Login failed: ${mainMessage}`
        });
        return;
      }

      const token = data.token || data.jwt || data.accessToken || data.access_token;
      if (token) {
        storeJwt(token);
      }

      const role = (data as any).role || (data as any).user?.role || '';
      if (role) {
        storeAuthRole(role);
      }

      storeAuthUser(values.email.trim());

      setApiMessage({
        type: 'success',
        text: data.message || 'Login successful. Redirecting to dashboard...'
      });

      setTimeout(() => {
        router.push('/dashboard');
      }, 400);
    } catch {
      setApiMessage({
        type: 'error',
        text: 'Login failed: We could not reach the server. Please try again.'
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="min-h-screen w-full bg-bg-main px-4 py-6 sm:px-6 lg:px-8">
      <section className="mx-auto grid min-h-[calc(100vh-3rem)] w-full max-w-6xl grid-cols-1 overflow-hidden rounded-[10px] border border-border-muted bg-transparent md:grid-cols-1 lg:grid-cols-2">
        <aside className="relative flex flex-col justify-center border-b border-border-muted p-6 sm:p-8 lg:border-b-0 lg:border-r lg:p-12">
          <div className="absolute inset-0 bg-gradient-to-br from-brand/15 via-transparent to-transparent" aria-hidden="true" />
          <div className="relative">
            <p className="mb-2 text-sm font-medium uppercase tracking-[0.12em] text-brand">PawnScan</p>
            <h1 className="text-3xl font-semibold leading-tight text-slate-100 sm:text-4xl">Welcome Back</h1>
            <p className="mt-4 max-w-xl text-base leading-relaxed text-slate-300">
              Access PawnScan to continue verifying high-value items and protect your transactions.
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
              <p className="text-sm">Secure access with your registered account.</p>
            </div>
          </div>
        </aside>

        <section className="flex items-center p-4 sm:p-8 lg:p-12">
          <div className="w-full rounded-[10px] border border-border-muted bg-slate-950/70 p-6 sm:p-8">
            <h2 className="text-2xl font-semibold text-slate-100">Sign In</h2>
            <p className="mt-2 text-sm text-slate-300">Use your registered email and password</p>

            <form className="mt-6 space-y-4" onSubmit={handleSubmit} noValidate aria-label="Login form">
              <div>
                <label htmlFor="email" className="mb-1 block text-sm font-medium text-slate-200">
                  Email Address
                </label>
                <input
                  id="email"
                  aria-label="Email Address"
                  type="email"
                  value={values.email}
                  onChange={(event) => setValues((current) => ({ ...current, email: event.target.value }))}
                  className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                  autoComplete="email"
                />
                {(submitAttempted || values.email.length > 0) && errors.email && (
                  <p className="mt-1 text-sm text-status-stolen">{errors.email}</p>
                )}
              </div>

              <div>
                <label htmlFor="password" className="mb-1 block text-sm font-medium text-slate-200">
                  Password
                </label>
                <div className="flex gap-2">
                  <input
                    id="password"
                    aria-label="Password"
                    type={isPasswordVisible ? 'text' : 'password'}
                    value={values.password}
                    onChange={(event) => setValues((current) => ({ ...current, password: event.target.value }))}
                    className="min-h-12 w-full rounded-[10px] border border-border-muted bg-slate-900 px-3 py-2 text-slate-100 transition focus-visible:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                    autoComplete="current-password"
                  />
                  <button
                    type="button"
                    aria-label={isPasswordVisible ? 'Hide password' : 'Show password'}
                    onClick={() => setIsPasswordVisible((current) => !current)}
                    className="inline-flex min-h-12 min-w-12 items-center justify-center rounded-[10px] border border-border-muted bg-slate-900 px-3 text-slate-200 transition hover:border-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/30"
                  >
                    <EyeIcon open={isPasswordVisible} />
                  </button>
                </div>
                {(submitAttempted || values.password.length > 0) && errors.password && (
                  <p className="mt-1 text-sm text-status-stolen">{errors.password}</p>
                )}
              </div>

              {apiMessage && (
                <div
                  role="status"
                  aria-live="polite"
                  className={`rounded-[10px] border px-3 py-2 text-sm ${
                    apiMessage.type === 'error'
                      ? 'border-status-stolen/50 bg-status-stolen/10 text-status-stolen'
                      : 'border-status-clean/40 bg-status-clean/10 text-status-clean'
                  }`}
                >
                  {apiMessage.text}
                </div>
              )}

              <button
                type="submit"
                aria-label="Login"
                disabled={!canSubmit}
                className="min-h-12 w-full rounded-[10px] bg-brand px-4 py-3 text-base font-semibold text-slate-950 transition hover:brightness-95 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/40 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {isSubmitting ? 'Signing in...' : 'Login'}
              </button>
            </form>

            <p className="mt-4 text-sm text-slate-300">
              No account yet?{' '}
              <Link href="/register" className="font-medium text-brand transition hover:underline">
                Register here.
              </Link>
            </p>
          </div>
        </section>
      </section>
    </main>
  );
}
