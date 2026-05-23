export const JWT_STORAGE_KEY = 'pawnscan_jwt';
export const AUTH_USER_KEY = 'pawnscan_auth_user';
export const AUTH_ROLE_KEY = 'pawnscan_auth_role';

export function storeJwt(token: string): void {
  if (typeof window === 'undefined') {
    return;
  }

  sessionStorage.setItem(JWT_STORAGE_KEY, token);
}

export function getJwt(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }

  return sessionStorage.getItem(JWT_STORAGE_KEY);
}

export function storeAuthUser(email: string): void {
  if (typeof window === 'undefined') {
    return;
  }

  sessionStorage.setItem(AUTH_USER_KEY, email);
}

export function getAuthUser(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }

  return sessionStorage.getItem(AUTH_USER_KEY);
}

export function storeAuthRole(role: string): void {
  if (typeof window === 'undefined') {
    return;
  }

  sessionStorage.setItem(AUTH_ROLE_KEY, role);
}

export function getAuthRole(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }

  return sessionStorage.getItem(AUTH_ROLE_KEY);
}

export function clearAuthSession(): void {
  if (typeof window === 'undefined') {
    return;
  }

  sessionStorage.removeItem(JWT_STORAGE_KEY);
  sessionStorage.removeItem(AUTH_USER_KEY);
  sessionStorage.removeItem(AUTH_ROLE_KEY);
}

export async function fetchMe() {
  const token = getJwt();
  if (!token) return null;

  try {
    const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080"}/api/auth/me`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    if (!res.ok) return null;
    return await res.json();
  } catch (err) {
    console.error("Failed to fetch me:", err);
    return null;
  }
}


