import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button, Alert } from '@mui/material';
import Modal from '../forms/Modal';
import FormField from '../forms/FormField';
import { createEmployee } from '../../api/employees';
import { AxiosError } from 'axios';

const schema = z.object({
  fullName: z.string().min(1, 'Imię i nazwisko jest wymagane'),
  email: z.string().min(1, 'Email jest wymagany').email('Nieprawidłowy format email'),
  password: z.string().min(1, 'Hasło jest wymagane'),
  role: z.enum(['ADMIN', 'EMPLOYEE'], { required_error: 'Rola jest wymagana' }),
  hiredFrom: z.string().min(1, 'Data zatrudnienia jest wymagana'),
  hiredUntil: z.string().optional(),
});

type FormData = z.infer<typeof schema>;

interface AddEmployeeModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export default function AddEmployeeModal({ open, onClose, onSuccess }: AddEmployeeModalProps) {
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      role: 'EMPLOYEE',
    },
  });

  const onSubmit = async (data: FormData) => {
    setLoading(true);
    setError(null);

    try {
      await createEmployee({
        ...data,
        hiredUntil: data.hiredUntil || null,
      });
      reset();
      onSuccess();
      onClose();
    } catch (err) {
      const axiosError = err as AxiosError<{ message: string }>;
      setError(axiosError.response?.data?.message || 'Wystąpił błąd podczas dodawania pracownika');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    reset();
    setError(null);
    onClose();
  };

  return (
    <Modal
      open={open}
      onClose={handleClose}
      title="Dodaj pracownika"
      actions={
        <>
          <Button onClick={handleClose} disabled={loading}>
            Anuluj
          </Button>
          <Button
            onClick={handleSubmit(onSubmit)}
            variant="contained"
            disabled={loading}
          >
            {loading ? 'Zapisywanie...' : 'Dodaj'}
          </Button>
        </>
      }
    >
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <form onSubmit={handleSubmit(onSubmit)}>
        <FormField
          label="Imię i nazwisko"
          name="fullName"
          register={register}
          error={errors.fullName}
          required
          autoFocus
        />
        <FormField
          label="Email"
          name="email"
          type="email"
          register={register}
          error={errors.email}
          required
        />
        <FormField
          label="Hasło"
          name="password"
          type="password"
          register={register}
          error={errors.password}
          required
        />
        <FormField
          label="Rola"
          name="role"
          type="select"
          register={register}
          error={errors.role}
          required
          options={[
            { value: 'EMPLOYEE', label: 'Pracownik' },
            { value: 'ADMIN', label: 'Administrator' },
          ]}
        />
        <FormField
          label="Data zatrudnienia od"
          name="hiredFrom"
          type="date"
          register={register}
          error={errors.hiredFrom}
          required
        />
        <FormField
          label="Data zatrudnienia do (opcjonalna)"
          name="hiredUntil"
          type="date"
          register={register}
          error={errors.hiredUntil}
        />
      </form>
    </Modal>
  );
}
