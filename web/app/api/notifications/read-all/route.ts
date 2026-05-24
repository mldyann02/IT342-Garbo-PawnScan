import { NextResponse } from "next/server";

const backendBaseUrl = process.env.BACKEND_URL || "http://localhost:8080";

export async function POST(request: Request) {
  const authorization = request.headers.get("authorization");
  if (!authorization) {
    return NextResponse.json({ message: "Unauthorized" }, { status: 401 });
  }

  try {
    const backendResponse = await fetch(`${backendBaseUrl}/api/notifications/read-all`, {
      method: "POST",
      headers: {
        Authorization: authorization,
      },
    });

    const data = await backendResponse.json().catch(() => ({}));
    return NextResponse.json(data, { status: backendResponse.status });
  } catch {
    return NextResponse.json({ message: "Unable to update notifications" }, { status: 503 });
  }
}
