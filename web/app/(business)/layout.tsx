import BusinessDashboardHeader from "@/features/business/components/business-dashboard-header";

export default function BusinessLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <>
      <BusinessDashboardHeader />
      {children}
    </>
  );
}
