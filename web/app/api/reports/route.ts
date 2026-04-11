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

  try {
    const backendResponse = await fetch(`${backendBaseUrl}/api/reports`, {
      method: "GET",
      headers: {
        Authorization: authorization,
      },
    });

    return mapBackendResponse(backendResponse);
  } catch {
    return NextResponse.json({ message: "Unable to fetch reports" }, { status: 503 });
  }
}

export async function POST(request: Request) {
  const authorization = request.headers.get("authorization");
  if (!authorization) {
    return buildUnauthorizedResponse();
  }

  try {
    const formData = await request.formData();

    const backendResponse = await fetch(`${backendBaseUrl}/api/reports`, {
      method: "POST",
      headers: {
        Authorization: authorization,
      },
      body: formData,
    });

    return mapBackendResponse(backendResponse);
  } catch {
    return NextResponse.json({ message: "Unable to create report" }, { status: 503 });
  }
}
