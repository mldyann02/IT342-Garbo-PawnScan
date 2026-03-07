import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import LoginForm from './login-form';

const pushMock = vi.fn();
let mockSearchParams = new URLSearchParams();

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: pushMock
  }),
  useSearchParams: () => mockSearchParams
}));

describe('LoginForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal('fetch', vi.fn());
    window.sessionStorage.clear();
    mockSearchParams = new URLSearchParams();
  });

  it('submits login successfully and redirects to dashboard', async () => {
    vi.mocked(fetch).mockResolvedValueOnce(new Response(JSON.stringify({ message: 'Login successful' }), { status: 200 }));

    render(<LoginForm />);

    await userEvent.type(screen.getByLabelText('Email Address'), 'user@example.com');
    await userEvent.type(screen.getByLabelText('Password'), 'Strong123');

    const submitButton = screen.getByRole('button', { name: 'Login' });
    expect(submitButton).toBeEnabled();

    await userEvent.click(submitButton);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        '/api/auth/login',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({
            email: 'user@example.com',
            password: 'Strong123'
          })
        })
      );
    });

    expect(window.sessionStorage.getItem('pawnscan_jwt')).toBeNull();
    expect(window.sessionStorage.getItem('pawnscan_auth_user')).toBe('user@example.com');

    await waitFor(() => {
      expect(pushMock).toHaveBeenCalledWith('/dashboard');
    });
  });

  it('shows login API error', async () => {
    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(JSON.stringify({ message: 'Invalid email or password' }), { status: 401 })
    );

    render(<LoginForm />);

    await userEvent.type(screen.getByLabelText('Email Address'), 'user@example.com');
    await userEvent.type(screen.getByLabelText('Password'), 'Strong123');
    await userEvent.click(screen.getByRole('button', { name: 'Login' }));

    expect(await screen.findByText(/Login failed: Invalid email or password/i)).toBeInTheDocument();
  });

  it('shows registration success message from query params', async () => {
    mockSearchParams = new URLSearchParams('registered=1&email=biz%40example.com&role=BUSINESS');

    render(<LoginForm />);

    expect(
      screen.getByText('Business account for biz@example.com registered successfully. Please log in.')
    ).toBeInTheDocument();
  });
});
