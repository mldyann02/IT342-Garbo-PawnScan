import { NextRequest, NextResponse } from 'next/server';

export async function PUT(request: NextRequest) {
  const backendBaseUrl = process.env.BACKEND_URL || 'http://localhost:8080';
  const token = request.cookies.get('pawnscan_jwt')?.value;
  const authorization = request.headers.get('authorization');

  try {
    const body = await request.json();

    const backendResponse = await fetch(`${backendBaseUrl}/api/auth/complete-profile`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(authorization ? { Authorization: authorization } : token ? { Authorization: `Bearer ${token}` } : {})
      },
      body: JSON.stringify(body)
    });

    const data = await backendResponse.json().catch(() => ({}));

    if (!backendResponse.ok) {
      const code =
        (data && typeof data === 'object' && 'code' in data && typeof data.code === 'string' && data.code) ||
        'AUTH-CP-001';

      return NextResponse.json(
        {
          ...data,
          code
        },
        { status: backendResponse.status }
      );
    }

    const response = NextResponse.json(data, { status: backendResponse.status });
    const newToken =
      data && typeof data === 'object'
        ? data.token || data.jwt || data.accessToken || data.access_token
        : null;

    if (typeof newToken === 'string' && newToken.length > 0) {
      response.cookies.set('pawnscan_jwt', newToken, {
        httpOnly: true,
        sameSite: 'lax',
        secure: process.env.NODE_ENV === 'production',
        path: '/',
        maxAge: 60 * 60 * 24
      });
    }

    return response;
  } catch {
    return NextResponse.json(
      {
        code: 'AUTH-CP-001',
        message: 'Unable to process profile completion request'
      },
      { status: 503 }
    );
  }
}
