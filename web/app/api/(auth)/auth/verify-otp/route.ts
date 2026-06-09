import { NextResponse } from "next/server";
import { cookies } from "next/headers";

const backendBaseUrl =
  process.env.BACKEND_URL ||
  process.env.NEXT_PUBLIC_BACKEND_URL ||
  "http://localhost:8080";
const backendApiUrl = backendBaseUrl.endsWith("/api")
  ? backendBaseUrl
  : `${backendBaseUrl.replace(/\/$/, "")}/api`;

export async function POST(request: Request) {
  try {
    const body = await request.json();

    const response = await fetch(`${backendApiUrl}/auth/verify-otp`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });

    const data = await response.json();

    if (!response.ok) {
      return NextResponse.json(
        { error: data.message || "Failed to verify OTP" },
        { status: response.status }
      );
    }

    if (data.token) {
      const cookieStore = cookies();
      cookieStore.set({
        name: "auth_token",
        value: data.token,
        httpOnly: true,
        path: "/",
        secure: process.env.NODE_ENV === "production",
        sameSite: "lax",
        maxAge: 60 * 60 * 24, // 1 day
      });
    }

    return NextResponse.json(data);
  } catch (error) {
    console.error("OTP verification error:", error);
    return NextResponse.json(
      { error: "Internal server error during verification" },
      { status: 500 }
    );
  }
}
