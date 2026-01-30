import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import EmptyState from './EmptyState';

describe('EmptyState', () => {
  it('should render the provided message', () => {
    const message = 'Brak danych do wyświetlenia';
    render(<EmptyState message={message} />);

    expect(screen.getByText(message)).toBeInTheDocument();
  });

  it('should render the inbox icon', () => {
    render(<EmptyState message="Test message" />);

    expect(screen.getByTestId('InboxOutlinedIcon')).toBeInTheDocument();
  });

  it('should render different messages correctly', () => {
    const { rerender } = render(<EmptyState message="First message" />);
    expect(screen.getByText('First message')).toBeInTheDocument();

    rerender(<EmptyState message="Second message" />);
    expect(screen.getByText('Second message')).toBeInTheDocument();
    expect(screen.queryByText('First message')).not.toBeInTheDocument();
  });

  it('should apply correct styling for empty state container', () => {
    const { container } = render(<EmptyState message="Test" />);

    const box = container.firstChild as HTMLElement;
    expect(box).toHaveStyle({ display: 'flex' });
  });
});
