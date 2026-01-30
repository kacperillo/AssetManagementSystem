import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import LoadingSpinner from './LoadingSpinner';

describe('LoadingSpinner', () => {
  it('should render a circular progress indicator', () => {
    render(<LoadingSpinner />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should be centered within its container', () => {
    const { container } = render(<LoadingSpinner />);

    const box = container.firstChild as HTMLElement;
    expect(box).toHaveStyle({
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
    });
  });

  it('should render without any text content', () => {
    const { container } = render(<LoadingSpinner />);

    const textContent = container.textContent;
    expect(textContent).toBe('');
  });
});
