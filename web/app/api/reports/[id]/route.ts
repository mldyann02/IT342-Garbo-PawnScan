import { NextResponse } from "next/server";

const backendBaseUrl = process.env.BACKEND_URL || "http://localhost:8080";

function buildUnauthorizedResponse() {
  return NextResponse.json({ message: "Unauthorized" }, { status: 401 });
}

async function mapBackendResponse(backendResponse: Response) {
  const data = await backendResponse.json().catch(() => ({}));
  return NextResponse.json(data, { status: backendResponse.status });
}

export async function PUT(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  const authorization = request.headers.get("authorization");
  if (!authorization) {
    return buildUnauthorizedResponse();
  }

  try {
    const { id } = await params;
    const formData = await request.formData();

    const backendResponse = await fetch(`${backendBaseUrl}/api/reports/${id}`, {
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

export async function DELETE(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  const authorization = request.headers.get("authorization");
  if (!authorization) {
    return buildUnauthorizedResponse();
  }

  try {
    const { id } = await params;

    const backendResponse = await fetch(`${backendBaseUrl}/api/reports/${id}`, {
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
