import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import RegisterForm from './register-form';

const pushMock = vi.fn();

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: pushMock
  })
}));

describe('RegisterForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal('fetch', vi.fn());
    window.sessionStorage.clear();
  });

  it('submits individual registration successfully', async () => {
    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(
        JSON.stringify({
          message: 'User registered',
          token: 'jwt-token',
          email: 'juan@example.com',
          role: 'USER'
        }),
        { status: 201 }
      )
    );

    render(<RegisterForm />);

    await userEvent.type(screen.getByLabelText('Full Name'), 'Juan Dela Cruz');
    await userEvent.type(screen.getByLabelText('Email Address'), 'juan@example.com');
    await userEvent.type(screen.getByLabelText('Password'), 'Strong123');
    await userEvent.type(screen.getByLabelText('Confirm Password'), 'Strong123');
    await userEvent.type(screen.getByLabelText('Contact Number'), '09171234567');

    const submitButton = screen.getByRole('button', { name: 'Register Account' });
    expect(submitButton).toBeEnabled();

    await userEvent.click(submitButton);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        '/api/auth/register',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({
            fullName: 'Juan Dela Cruz',
            email: 'juan@example.com',
            password: 'Strong123',
            phoneNumber: '09171234567',
            role: 'INDIVIDUAL'
          })
        })
      );
    });

    expect(window.sessionStorage.getItem('pawnscan_jwt')).toBe('jwt-token');

    await waitFor(() => {
      expect(pushMock).toHaveBeenCalledWith('/login?registered=1&email=juan%40example.com&role=USER');
    });
  });

  it('requires business fields for business role', async () => {
    render(<RegisterForm />);

    await userEvent.click(screen.getByRole('button', { name: 'Select Business account' }));

    expect(screen.queryByLabelText('Full Name')).not.toBeInTheDocument();

    await userEvent.type(screen.getByLabelText('Business Name'), 'Pawn Business Inc.');
    await userEvent.type(screen.getByLabelText('Email Address'), 'biz@example.com');
    await userEvent.type(screen.getByLabelText('Contact Number'), '09170000000');
    await userEvent.type(screen.getByLabelText('Business Address'), 'Cebu City');
    await userEvent.type(screen.getByLabelText('Permit Number'), 'PN-1234');
    await userEvent.type(screen.getByLabelText('Password'), 'Strong123');
    await userEvent.type(screen.getByLabelText('Confirm Password'), 'Strong123');

    const submitButton = screen.getByRole('button', { name: 'Register Account' });
    expect(submitButton).toBeEnabled();

    vi.mocked(fetch).mockResolvedValueOnce(new Response(JSON.stringify({ message: 'ok' }), { status: 201 }));
    await userEvent.click(submitButton);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        '/api/auth/register',
        expect.objectContaining({
          body: JSON.stringify({
            fullName: 'Pawn Business Inc.',
            email: 'biz@example.com',
            password: 'Strong123',
            phoneNumber: '09170000000',
            role: 'BUSINESS',
            business_name: 'Pawn Business Inc.',
            business_address: 'Cebu City',
            permit_number: 'PN-1234'
          })
        })
      );
    });
  });

  it('disables submit when passwords do not match', async () => {
    render(<RegisterForm />);

    await userEvent.type(screen.getByLabelText('Full Name'), 'Mismatch User');
    await userEvent.type(screen.getByLabelText('Email Address'), 'mismatch@example.com');
    await userEvent.type(screen.getByLabelText('Contact Number'), '09171234567');
    await userEvent.type(screen.getByLabelText('Password'), 'Strong123');
    await userEvent.type(screen.getByLabelText('Confirm Password'), 'Strong321');

    const submitButton = screen.getByRole('button', { name: 'Register Account' });
    expect(submitButton).toBeDisabled();
    expect(screen.getByText(/Passwords do not match./i)).toBeInTheDocument();
  });

  it('shows API error message with code', async () => {
    vi.mocked(fetch).mockResolvedValueOnce(
      new Response(JSON.stringify({ code: 'VALID-001', message: 'Email already in use.' }), { status: 400 })
    );

    render(<RegisterForm />);

    await userEvent.type(screen.getByLabelText('Full Name'), 'Juan Dela Cruz');
    await userEvent.type(screen.getByLabelText('Email Address'), 'juan@example.com');
    await userEvent.type(screen.getByLabelText('Password'), 'Strong123');
    await userEvent.type(screen.getByLabelText('Confirm Password'), 'Strong123');
    await userEvent.type(screen.getByLabelText('Contact Number'), '09171234567');

    await userEvent.click(screen.getByRole('button', { name: 'Register Account' }));

    expect(await screen.findByText(/Registration failed: Email already in use./i)).toBeInTheDocument();
  });
});
