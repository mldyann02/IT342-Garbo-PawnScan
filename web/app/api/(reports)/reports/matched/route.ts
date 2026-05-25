import { NextResponse } from "next/server";

const backendBaseUrl = process.env.BACKEND_URL || "http://localhost:8080";

export async function GET(request: Request) {
  const authorization = request.headers.get("authorization");
  if (!authorization) {
    return NextResponse.json({ message: "Unauthorized" }, { status: 401 });
  }

  const { searchParams } = new URL(request.url);
  const page = searchParams.get("page") || "0";
  const size = searchParams.get("size") || "20";

  try {
    const backendResponse = await fetch(
      `${backendBaseUrl}/api/reports/matched?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}`,
      {
        method: "GET",
        headers: {
          Authorization: authorization,
        },
      },
    );

    const data = await backendResponse.json().catch(() => ({}));
    return NextResponse.json(data, { status: backendResponse.status });
  } catch {
    return NextResponse.json({ message: "Unable to fetch matched reports" }, { status: 503 });
  }
}
