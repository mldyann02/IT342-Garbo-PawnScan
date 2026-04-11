import { NextResponse } from "next/server";

const backendBaseUrl = process.env.BACKEND_URL || "http://localhost:8080";

function isAbsoluteUrl(value: string) {
  return /^https?:\/\//i.test(value);
}

function normalizeBackendEvidenceUrl(source: string): string | null {
  const trimmed = source.trim();
  if (!trimmed) {
    return null;
  }

  if (isAbsoluteUrl(trimmed)) {
    try {
      const parsed = new URL(trimmed);
      // Only proxy backend uploads for safety.
      if (!parsed.pathname.startsWith("/uploads/")) {
        return null;
      }
      return trimmed;
    } catch {
      return null;
    }
  }

  // Relative values from backend are expected to be /uploads/...
  if (!trimmed.startsWith("/uploads/")) {
    return null;
  }

  return `${backendBaseUrl.replace(/\/$/, "")}${trimmed}`;
}

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const source = searchParams.get("source") || "";

  const targetUrl = normalizeBackendEvidenceUrl(source);
  if (!targetUrl) {
    return NextResponse.json(
      { message: "Invalid evidence source" },
      { status: 400 },
    );
  }

  try {
    const backendResponse = await fetch(targetUrl, {
      method: "GET",
      cache: "no-store",
    });

    if (!backendResponse.ok) {
      return NextResponse.json(
        { message: "Unable to fetch evidence file" },
        { status: backendResponse.status },
      );
    }

    const contentType =
      backendResponse.headers.get("content-type") ||
      "application/octet-stream";
    const data = await backendResponse.arrayBuffer();

    return new NextResponse(data, {
      status: 200,
      headers: {
        "Content-Type": contentType,
        "Cache-Control": "private, max-age=60",
      },
    });
  } catch {
    return NextResponse.json(
      { message: "Unable to fetch evidence file" },
      { status: 503 },
    );
  }
}
