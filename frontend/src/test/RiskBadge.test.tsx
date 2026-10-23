import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { RiskBadge } from '../components/common/RiskBadge';

describe('RiskBadge Component', () => {
  it('renders LOW risk badge with score', () => {
    render(<RiskBadge score={15} level="LOW" />);
    expect(screen.getByText('LOW')).toBeInTheDocument();
    expect(screen.getByText('(15)')).toBeInTheDocument();
  });

  it('renders MEDIUM risk badge with score', () => {
    render(<RiskBadge score={55} level="MEDIUM" />);
    expect(screen.getByText('MEDIUM')).toBeInTheDocument();
    expect(screen.getByText('(55)')).toBeInTheDocument();
  });

  it('renders HIGH risk badge with score', () => {
    render(<RiskBadge score={85} level="HIGH" />);
    expect(screen.getByText('HIGH')).toBeInTheDocument();
    expect(screen.getByText('(85)')).toBeInTheDocument();
  });

  it('hides score when showScore is false', () => {
    render(<RiskBadge score={85} level="HIGH" showScore={false} />);
    expect(screen.getByText('HIGH')).toBeInTheDocument();
    expect(screen.queryByText('(85)')).not.toBeInTheDocument();
  });
});
