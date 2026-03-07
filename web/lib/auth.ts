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
