import { NextResponse } from 'next/server';

type BackendGoogleConfigResponse = {
  configured?: boolean;
  clientId?: string | null;
};

export async function GET() {
  const localClientId =
    process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID || process.env.GOOGLE_CLIENT_ID || '';

  if (localClientId.trim()) {
    return NextResponse.json({
      configured: true,
      clientId: localClientId.trim()
    });
  }

  const backendBaseUrl = process.env.BACKEND_URL || 'http://localhost:8080';

  try {
    const backendResponse = await fetch(`${backendBaseUrl}/api/auth/google/config`, {
      cache: 'no-store'
    });
    const data = (await backendResponse.json().catch(() => ({}))) as BackendGoogleConfigResponse;
    const backendClientId = typeof data.clientId === 'string' ? data.clientId.trim() : '';

    return NextResponse.json({
      configured: Boolean(data.configured && backendClientId),
      clientId: backendClientId || null
    });
  } catch {
    return NextResponse.json({
      configured: false,
      clientId: null
    });
  }
}
