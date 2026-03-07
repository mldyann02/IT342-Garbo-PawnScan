import { NextResponse } from 'next/server';

type RequestBody = {
  fullName: string;
  email: string;
  password: string;
  phoneNumber: string;
  role: 'INDIVIDUAL' | 'BUSINESS';
  business_name?: string;
  business_address?: string;
  permit_number?: string;
};

export async function POST(request: Request) {
  const backendBaseUrl = process.env.BACKEND_URL || 'http://localhost:8080';

  try {
    const body = (await request.json()) as RequestBody;
    const backendRole = body.role === 'BUSINESS' ? 'BUSINESS' : 'USER';

    const backendResponse = await fetch(`${backendBaseUrl}/api/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        fullName: body.fullName,
        email: body.email,
        password: body.password,
        phoneNumber: body.phoneNumber,
        role: backendRole,
        business_name: body.business_name,
        business_address: body.business_address,
        permit_number: body.permit_number
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
        message: 'Unable to process registration request'
      },
      { status: 503 }
    );
  }
}
