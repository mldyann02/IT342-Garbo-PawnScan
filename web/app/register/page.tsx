import RegisterForm from "@/components/register-form";
import Header from "@/components/header";

export default function RegisterPage() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-bg-main to-[#071022] pt-16 sm:pt-20">
      <Header />
      <RegisterForm />
    </div>
  );
}
