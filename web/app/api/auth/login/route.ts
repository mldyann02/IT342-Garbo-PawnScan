import { NextResponse } from 'next/server';

type RequestBody = {
  email: string;
  password: string;
};

export async function POST(request: Request) {
  const backendBaseUrl = process.env.BACKEND_URL || 'http://localhost:8080';

  try {
    const body = (await request.json()) as RequestBody;

    const backendResponse = await fetch(`${backendBaseUrl}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email: body.email,
        password: body.password
      })
    });

    const data = await backendResponse.json().catch(() => ({}));

    if (!backendResponse.ok) {
      const code =
        (data && typeof data === 'object' && 'code' in data && typeof data.code === 'string' && data.code) ||
        (backendResponse.status === 400 ? 'VALID-001' : 'AUTH-001');

      return NextResponse.json(
        {
          ...data,
          code
        },
        { status: backendResponse.status }
      );
    }

    return NextResponse.json(data, { status: backendResponse.status });
  } catch {
    return NextResponse.json(
      {
        code: 'AUTH-001',
        message: 'Unable to process login request'
      },
      { status: 503 }
    );
  }
}
