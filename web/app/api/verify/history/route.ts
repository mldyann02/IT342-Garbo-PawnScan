import { NextResponse } from "next/server";

const backendBaseUrl = process.env.BACKEND_URL || "http://localhost:8080";

function buildUnauthorizedResponse() {
  return NextResponse.json({ message: "Unauthorized" }, { status: 401 });
}

async function mapBackendResponse(backendResponse: Response) {
  const data = await backendResponse.json().catch(() => ({}));
  return NextResponse.json(data, { status: backendResponse.status });
}

export async function GET(request: Request) {
  const authorization = request.headers.get("authorization");
  if (!authorization) {
    return buildUnauthorizedResponse();
  }

  const { searchParams } = new URL(request.url);
  const page = searchParams.get("page") || "0";
  const size = searchParams.get("size") || "20";

  try {
    const backendResponse = await fetch(
      `${backendBaseUrl}/api/verify/history?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}`,
      {
        method: "GET",
        headers: {
          Authorization: authorization,
        },
      },
    );

    return mapBackendResponse(backendResponse);
  } catch {
    return NextResponse.json(
      { message: "Unable to fetch search history" },
      { status: 503 },
    );
  }
}
