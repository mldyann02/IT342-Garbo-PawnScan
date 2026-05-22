import RegisterForm from "@/features/auth/components/register-form";
import { Suspense } from "react";

export default function RegisterPage() {
  return (
    <div className="min-h-screen pt-16 sm:pt-20">
      <Suspense fallback={<div className="min-h-screen" />}>
        <RegisterForm />
      </Suspense>
    </div>
  );
}


