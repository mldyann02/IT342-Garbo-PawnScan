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
      <body className="min-h-screen bg-bg-main">
        <div className="min-h-screen w-full flex flex-col text-slate-100">
          <main className="flex-grow">{children}</main>
        </div>
      </body>
    </html>
  );
}
