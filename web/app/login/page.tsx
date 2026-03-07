import Link from 'next/link';

export default function LoginPage() {
  return (
    <main className="flex min-h-screen items-center justify-center p-6">
      <section className="w-full max-w-md rounded-xl border border-border-muted bg-white p-6 text-center shadow-sm">
        <h1 className="text-xl font-semibold">Login Page</h1>
        <p className="mt-2 text-sm text-slate-600">Login implementation can be added here.</p>
        <Link href="/register" className="mt-4 inline-block text-brand underline-offset-2 hover:underline">
          Back to Register
        </Link>
      </section>
    </main>
  );
}
