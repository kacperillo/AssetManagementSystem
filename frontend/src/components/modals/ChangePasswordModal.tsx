import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button, Alert } from '@mui/material';
import Modal from '../forms/Modal';
import FormField from '../forms/FormField';
import { changePassword } from '../../api/auth';
import { useAuth } from '../../hooks/useAuth';
import { AxiosError } from 'axios';

const schema = z.object({
  currentPassword: z.string().min(1, 'Aktualne hasło jest wymagane'),
  newPassword: z.string().min(1, 'Nowe hasło jest wymagane'),
});

type FormData = z.infer<typeof schema>;

interface ChangePasswordModalProps {
  open: boolean;
  onClose: () => void;
}

export default function ChangePasswordModal({ open, onClose }: ChangePasswordModalProps) {
  const { user } = useAuth();
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data: FormData) => {
    if (!user) return;

    setLoading(true);
    setError(null);

    try {
      await changePassword({
        email: user.email,
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
      });
      setSuccess(true);
      reset();
      setTimeout(() => {
        onClose();
        setSuccess(false);
      }, 2000);
    } catch (err) {
      const axiosError = err as AxiosError<{ message: string }>;
      setError(axiosError.response?.data?.message || 'Wystąpił błąd podczas zmiany hasła');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    reset();
    setError(null);
    setSuccess(false);
    onClose();
  };

  return (
    <Modal
      open={open}
      onClose={handleClose}
      title="Zmień hasło"
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
            {loading ? 'Zapisywanie...' : 'Zmień hasło'}
          </Button>
        </>
      }
    >
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>Hasło zostało zmienione</Alert>}

      <form onSubmit={handleSubmit(onSubmit)}>
        <FormField
          label="Aktualne hasło"
          name="currentPassword"
          type="password"
          register={register}
          error={errors.currentPassword}
          required
        />
        <FormField
          label="Nowe hasło"
          name="newPassword"
          type="password"
          register={register}
          error={errors.newPassword}
          required
        />
      </form>
    </Modal>
  );
}
