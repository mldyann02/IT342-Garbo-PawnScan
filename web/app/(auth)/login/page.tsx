import LoginForm from "@/features/auth/components/login-form";
import Header from "@/features/marketing/components/header";
import { Suspense } from "react";

export default function LoginPage() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-bg-main to-[#071022] pt-16 sm:pt-20">
      <Header />
      <Suspense fallback={<div className="min-h-screen" />}>
        <LoginForm />
      </Suspense>
    </div>
  );
}


