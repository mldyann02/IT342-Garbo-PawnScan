import { Metadata } from "next";
import { Suspense } from "react";
import ResetPasswordForm from "@/features/auth/components/reset-password-form";

export const metadata: Metadata = {
  title: "Reset Password | PawnScan",
  description:
    "Create a new password for your PawnScan account using your secure reset link.",
};

export default function ResetPasswordPage() {
  return (
    <Suspense>
      <ResetPasswordForm />
    </Suspense>
  );
}
