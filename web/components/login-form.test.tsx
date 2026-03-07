import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import LoginForm from './login-form';

const pushMock = vi.fn();

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: pushMock
  })
}));

describe('LoginForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal('fetch', vi.fn());
    window.sessionStorage.clear();
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

    expect(window.sessionStorage.getItem('pawnscan_jwt')).toBeTruthy();
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
});
