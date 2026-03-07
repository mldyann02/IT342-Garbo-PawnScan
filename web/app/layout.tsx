import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "PawnScan",
  description: "PawnScan web application",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>
        <div className="min-h-screen w-full flex flex-col bg-[inherit] text-slate-100">
          {children}
        </div>
      </body>
    </html>
  );
}
