const backendBaseUrl = process.env.BACKEND_URL || "http://localhost:8080";

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const token = searchParams.get("token") || "";

  try {
    const backendResponse = await fetch(
      `${backendBaseUrl}/api/notifications/stream?token=${encodeURIComponent(token)}`,
      {
        method: "GET",
        headers: {
          Accept: "text/event-stream",
        },
      },
    );

    return new Response(backendResponse.body, {
      status: backendResponse.status,
      headers: {
        "Content-Type": "text/event-stream",
        "Cache-Control": "no-cache, no-transform",
        Connection: "keep-alive",
      },
    });
  } catch {
    return new Response(null, { status: 204 });
  }
}
