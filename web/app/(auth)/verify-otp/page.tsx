import { Metadata } from "next";
import { VerifyOtpForm } from "@/features/auth/components/verify-otp-form";

export const metadata: Metadata = {
  title: "Verify Email - PawnScan",
  description: "Verify your email to continue",
};

export default function VerifyOtpPage() {
  return (
    <main className="relative flex min-h-[calc(100vh-88px)] w-full items-center justify-center px-4 py-12 sm:px-6 lg:px-8">
      <div className="w-full max-w-sm">
        <VerifyOtpForm />
      </div>
    </main>
  );
}
