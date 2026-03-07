export const JWT_STORAGE_KEY = 'pawnscan_jwt';

export function storeJwt(token: string): void {
  if (typeof window === 'undefined') {
    return;
  }

  sessionStorage.setItem(JWT_STORAGE_KEY, token);
}
