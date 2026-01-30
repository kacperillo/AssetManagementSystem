import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import ErrorMessage from './ErrorMessage';

describe('ErrorMessage', () => {
  it('should render the provided error message', () => {
    const message = 'Wystąpił błąd podczas ładowania danych';
    render(<ErrorMessage message={message} />);

    expect(screen.getByText(message)).toBeInTheDocument();
  });

  it('should render as an error alert', () => {
    render(<ErrorMessage message="Test error" />);

    const alert = screen.getByRole('alert');
    expect(alert).toBeInTheDocument();
    // MUI Alert with severity="error" has error-related classes
    expect(alert.className).toMatch(/error/i);
  });

  it('should render different error messages correctly', () => {
    const { rerender } = render(<ErrorMessage message="First error" />);
    expect(screen.getByText('First error')).toBeInTheDocument();

    rerender(<ErrorMessage message="Second error" />);
    expect(screen.getByText('Second error')).toBeInTheDocument();
    expect(screen.queryByText('First error')).not.toBeInTheDocument();
  });

  it('should display error icon', () => {
    render(<ErrorMessage message="Error with icon" />);

    const alert = screen.getByRole('alert');
    const icon = alert.querySelector('.MuiAlert-icon');
    expect(icon).toBeInTheDocument();
  });
});
