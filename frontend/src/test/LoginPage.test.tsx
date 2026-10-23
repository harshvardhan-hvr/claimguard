import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { LoginPage } from '../pages/LoginPage';
import { AuthProvider } from '../context/AuthContext';

describe('LoginPage Component', () => {
  it('renders login form inputs and demo account pill buttons', () => {
    render(
      <AuthProvider>
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      </AuthProvider>
    );

    expect(screen.getByText('ClaimGuard System')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('e.g. admin or reviewer1')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('••••••••')).toBeInTheDocument();
    expect(screen.getByText('Sign In to ClaimGuard')).toBeInTheDocument();
    expect(screen.getByText('Administrator')).toBeInTheDocument();
    expect(screen.getByText('Senior Reviewer (Sarah Connor)')).toBeInTheDocument();
  });

  it('clicking demo button populates username and password fields', () => {
    render(
      <AuthProvider>
        <BrowserRouter>
          <LoginPage />
        </BrowserRouter>
      </AuthProvider>
    );

    const adminBtn = screen.getByText('Administrator').closest('button');
    expect(adminBtn).toBeInTheDocument();
    fireEvent.click(adminBtn!);

    const userInput = screen.getByPlaceholderText('e.g. admin or reviewer1') as HTMLInputElement;
    const passInput = screen.getByPlaceholderText('••••••••') as HTMLInputElement;

    expect(userInput.value).toBe('admin');
    expect(passInput.value).toBe('password123');
  });
});
