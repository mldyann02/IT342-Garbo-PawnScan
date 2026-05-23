import LoginForm from "@/features/auth/components/login-form";
import { Suspense } from "react";

export default function LoginPage() {
  return (
    <div className="min-h-screen pt-16 sm:pt-20">
      <Suspense fallback={<div className="min-h-screen" />}>
        <LoginForm />
      </Suspense>
    </div>
  );
}


