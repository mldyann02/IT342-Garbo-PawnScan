import { NextResponse } from "next/server";

const backendBaseUrl = process.env.BACKEND_URL || "http://localhost:8080";

function buildUnauthorizedResponse() {
  return NextResponse.json({ message: "Unauthorized" }, { status: 401 });
}

async function mapBackendResponse(backendResponse: Response) {
  const text = await backendResponse.text().catch(() => "");

  if (!text) {
    return new NextResponse(null, { status: backendResponse.status });
  }

  try {
    return NextResponse.json(JSON.parse(text), { status: backendResponse.status });
  } catch {
    return new NextResponse(text, {
      status: backendResponse.status,
      headers: {
        "Content-Type": backendResponse.headers.get("content-type") || "text/plain",
      },
    });
  }
}

export async function PUT(request: Request, { params }: { params: { id: string } }) {
  const authorization = request.headers.get("authorization");
  if (!authorization) {
    return buildUnauthorizedResponse();
  }

  try {
    const formData = await request.formData();

    const backendResponse = await fetch(`${backendBaseUrl}/api/reports/${params.id}`, {
      method: "PUT",
      headers: {
        Authorization: authorization,
      },
      body: formData,
    });

    return mapBackendResponse(backendResponse);
  } catch {
    return NextResponse.json({ message: "Unable to update report" }, { status: 503 });
  }
}

export async function DELETE(request: Request, { params }: { params: { id: string } }) {
  const authorization = request.headers.get("authorization");
  if (!authorization) {
    return buildUnauthorizedResponse();
  }

  try {
    const backendResponse = await fetch(`${backendBaseUrl}/api/reports/${params.id}`, {
      method: "DELETE",
      headers: {
        Authorization: authorization,
      },
    });

    return mapBackendResponse(backendResponse);
  } catch {
    return NextResponse.json({ message: "Unable to delete report" }, { status: 503 });
  }
}