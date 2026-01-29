import { TextField, MenuItem } from '@mui/material';
import { UseFormRegister, FieldError } from 'react-hook-form';

interface Option {
  value: string;
  label: string;
}

interface FormFieldProps {
  label: string;
  name: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  register: UseFormRegister<any>;
  error?: FieldError;
  type?: 'text' | 'email' | 'password' | 'date' | 'select';
  required?: boolean;
  options?: Option[];
  autoFocus?: boolean;
}

export default function FormField({
  label,
  name,
  register,
  error,
  type = 'text',
  required = false,
  options = [],
  autoFocus = false,
}: FormFieldProps) {
  if (type === 'select') {
    return (
      <TextField
        select
        label={label}
        {...register(name)}
        error={!!error}
        helperText={error?.message}
        fullWidth
        margin="normal"
        required={required}
        autoFocus={autoFocus}
      >
        {options.map((option) => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </TextField>
    );
  }

  return (
    <TextField
      label={label}
      type={type}
      {...register(name)}
      error={!!error}
      helperText={error?.message}
      fullWidth
      margin="normal"
      required={required}
      autoFocus={autoFocus}
      slotProps={type === 'date' ? { inputLabel: { shrink: true } } : undefined}
    />
  );
}
