import { NextResponse } from 'next/server';

type RequestBody = {
  token: string;
  role?: string;
};

export async function POST(request: Request) {
  const backendBaseUrl = process.env.BACKEND_URL || 'http://localhost:8080';

  try {
    const body = (await request.json()) as RequestBody;

    const backendResponse = await fetch(`${backendBaseUrl}/api/auth/google`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        token: body.token,
        role: body.role
      })
    });

    const data = await backendResponse.json().catch(() => ({}));

    if (!backendResponse.ok) {
      const code =
        (data && typeof data === 'object' && 'code' in data && typeof data.code === 'string' && data.code) ||
        'AUTH-GOOGLE-001';

      return NextResponse.json(
        {
          ...data,
          code
        },
        { status: backendResponse.status }
      );
    }

    const response = NextResponse.json(data, { status: backendResponse.status });
    const token =
      data && typeof data === 'object'
        ? data.token || data.jwt || data.accessToken || data.access_token
        : null;

    if (typeof token === 'string' && token.length > 0) {
      response.cookies.set('pawnscan_jwt', token, {
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
        code: 'AUTH-GOOGLE-001',
        message: 'Unable to process Google authentication request'
      },
      { status: 503 }
    );
  }
}
