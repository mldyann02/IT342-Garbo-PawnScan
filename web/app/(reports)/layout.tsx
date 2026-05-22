import UserDashboardHeader from "@/features/dashboard/components/user-dashboard-header";

export default function ReportsLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <>
      <UserDashboardHeader />
      {children}
    </>
  );
}
