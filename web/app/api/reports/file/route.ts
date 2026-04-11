import { NextResponse } from "next/server";

const backendBaseUrl = process.env.BACKEND_URL || "http://localhost:8080";

export async function GET(request: Request) {
  const url = new URL(request.url);
  const path = url.searchParams.get("path");
  const token = url.searchParams.get("token");

  if (!path || !path.startsWith("/")) {
    return NextResponse.json({ message: "Invalid file path" }, { status: 400 });
  }

  const authorization = token
    ? `Bearer ${token}`
    : request.headers.get("authorization");

  try {
    const backendResponse = await fetch(`${backendBaseUrl}${path}`, {
      method: "GET",
      headers: authorization ? { Authorization: authorization } : undefined,
    });

    if (!backendResponse.ok) {
      const text = await backendResponse.text().catch(() => "");
      return NextResponse.json(
        { message: text || "Unable to fetch file" },
        { status: backendResponse.status },
      );
    }

    const body = await backendResponse.arrayBuffer();
    const contentType = backendResponse.headers.get("content-type") || "application/octet-stream";

    return new Response(body, {
      status: 200,
      headers: {
        "Content-Type": contentType,
        "Content-Disposition": "inline",
        "Cache-Control": "no-store",
      },
    });
  } catch {
    return NextResponse.json({ message: "Unable to fetch file" }, { status: 503 });
  }
}
