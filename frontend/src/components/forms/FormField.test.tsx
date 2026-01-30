import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { useForm } from 'react-hook-form';
import FormField from './FormField';

const FormFieldWrapper = ({
  type = 'text' as const,
  error,
  options,
  required = false,
  autoFocus = false,
}: {
  type?: 'text' | 'email' | 'password' | 'date' | 'select';
  error?: { message: string; type: string };
  options?: { value: string; label: string }[];
  required?: boolean;
  autoFocus?: boolean;
}) => {
  const { register } = useForm();

  return (
    <FormField
      label="Test Field"
      name="testField"
      register={register}
      type={type}
      error={error}
      options={options}
      required={required}
      autoFocus={autoFocus}
    />
  );
};

describe('FormField', () => {
  describe('Text Input', () => {
    it('should render text input with label', () => {
      render(<FormFieldWrapper />);

      expect(screen.getByLabelText(/Test Field/i)).toBeInTheDocument();
    });

    it('should render with text type by default', () => {
      render(<FormFieldWrapper />);

      const input = screen.getByLabelText(/Test Field/i);
      expect(input).toHaveAttribute('type', 'text');
    });
  });

  describe('Email Input', () => {
    it('should render email input', () => {
      render(<FormFieldWrapper type="email" />);

      const input = screen.getByLabelText(/Test Field/i);
      expect(input).toHaveAttribute('type', 'email');
    });
  });

  describe('Password Input', () => {
    it('should render password input', () => {
      render(<FormFieldWrapper type="password" />);

      const input = screen.getByLabelText(/Test Field/i);
      expect(input).toHaveAttribute('type', 'password');
    });
  });

  describe('Date Input', () => {
    it('should render date input', () => {
      render(<FormFieldWrapper type="date" />);

      const input = screen.getByLabelText(/Test Field/i);
      expect(input).toHaveAttribute('type', 'date');
    });
  });

  describe('Select Input', () => {
    it('should render select input with options', () => {
      const options = [
        { value: 'option1', label: 'Option 1' },
        { value: 'option2', label: 'Option 2' },
      ];

      render(<FormFieldWrapper type="select" options={options} />);

      expect(screen.getByLabelText(/Test Field/i)).toBeInTheDocument();
    });
  });

  describe('Error Handling', () => {
    it('should display error message when error is provided', () => {
      const error = { message: 'This field is required', type: 'required' };

      render(<FormFieldWrapper error={error} />);

      expect(screen.getByText('This field is required')).toBeInTheDocument();
    });

    it('should not display error message when no error', () => {
      render(<FormFieldWrapper />);

      expect(screen.queryByText(/error/i)).not.toBeInTheDocument();
    });

    it('should show error state on input when error is provided', () => {
      const error = { message: 'Invalid input', type: 'validation' };

      render(<FormFieldWrapper error={error} />);

      const input = screen.getByLabelText(/Test Field/i);
      expect(input).toHaveAttribute('aria-invalid', 'true');
    });
  });

  describe('Required Field', () => {
    it('should mark field as required when required prop is true', () => {
      render(<FormFieldWrapper required={true} />);

      const input = screen.getByLabelText(/Test Field/i);
      expect(input).toBeRequired();
    });

    it('should not mark field as required when required prop is false', () => {
      render(<FormFieldWrapper required={false} />);

      const input = screen.getByLabelText(/Test Field/i);
      expect(input).not.toBeRequired();
    });
  });

  describe('AutoFocus', () => {
    it('should have autoFocus when autoFocus prop is true', () => {
      render(<FormFieldWrapper autoFocus={true} />);

      const input = screen.getByLabelText(/Test Field/i);
      expect(document.activeElement).toBe(input);
    });
  });
});
