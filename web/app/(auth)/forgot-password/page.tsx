import { Metadata } from "next";
import ForgotPasswordForm from "@/features/auth/components/forgot-password-form";

export const metadata: Metadata = {
  title: "Forgot Password | PawnScan",
  description:
    "Enter your registered email address to receive a secure password reset link for your PawnScan account.",
};

export default function ForgotPasswordPage() {
  return <ForgotPasswordForm />;
}
