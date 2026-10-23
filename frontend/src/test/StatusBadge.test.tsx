import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { StatusBadge } from '../components/common/StatusBadge';

describe('StatusBadge Component', () => {
  it('renders Submitted status correctly', () => {
    render(<StatusBadge status="SUBMITTED" />);
    expect(screen.getByText('Submitted')).toBeInTheDocument();
  });

  it('renders Under Review status correctly', () => {
    render(<StatusBadge status="UNDER_REVIEW" />);
    expect(screen.getByText('Under Review')).toBeInTheDocument();
  });

  it('renders Flagged status correctly', () => {
    render(<StatusBadge status="FLAGGED" />);
    expect(screen.getByText('Flagged')).toBeInTheDocument();
  });

  it('renders Cleared status correctly', () => {
    render(<StatusBadge status="CLEARED" />);
    expect(screen.getByText('Cleared')).toBeInTheDocument();
  });

  it('renders Confirmed Fraud status correctly', () => {
    render(<StatusBadge status="CONFIRMED_FRAUD" />);
    expect(screen.getByText('Confirmed Fraud')).toBeInTheDocument();
  });
});
