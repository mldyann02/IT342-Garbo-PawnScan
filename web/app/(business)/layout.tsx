import BusinessDashboardHeader from "@/features/business/components/business-dashboard-header";
import VerificationGuard from "@/features/business/components/verification-guard";

export default function BusinessLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <VerificationGuard>
      <BusinessDashboardHeader />
      {children}
    </VerificationGuard>
  );
}
