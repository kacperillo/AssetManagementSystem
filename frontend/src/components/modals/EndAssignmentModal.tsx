import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button, Alert, Typography } from '@mui/material';
import Modal from '../forms/Modal';
import FormField from '../forms/FormField';
import { endAssignment, Assignment } from '../../api/assignments';
import { AxiosError } from 'axios';

const schema = z.object({
  assignedUntil: z.string().min(1, 'Data zakończenia jest wymagana'),
});

type FormData = z.infer<typeof schema>;

interface EndAssignmentModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
  assignment: Assignment | null;
}

export default function EndAssignmentModal({
  open,
  onClose,
  onSuccess,
  assignment,
}: EndAssignmentModalProps) {
  const [error, setError] = useState<string | null>(null);
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
    if (!assignment) return;

    setLoading(true);
    setError(null);

    try {
      await endAssignment(assignment.id, {
        assignedUntil: data.assignedUntil,
      });
      reset();
      onSuccess();
      onClose();
    } catch (err) {
      const axiosError = err as AxiosError<{ message: string }>;
      setError(axiosError.response?.data?.message || 'Wystąpił błąd podczas kończenia przydziału');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    reset();
    setError(null);
    onClose();
  };

  if (!assignment) return null;

  return (
    <Modal
      open={open}
      onClose={handleClose}
      title="Zakończ przydział"
      actions={
        <>
          <Button onClick={handleClose} disabled={loading}>
            Anuluj
          </Button>
          <Button
            onClick={handleSubmit(onSubmit)}
            variant="contained"
            color="warning"
            disabled={loading}
          >
            {loading ? 'Zapisywanie...' : 'Zakończ przydział'}
          </Button>
        </>
      }
    >
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Typography variant="body2" sx={{ mb: 2 }}>
        Czy na pewno chcesz zakończyć przydział zasobu <strong>{assignment.vendor} {assignment.model}</strong> dla pracownika <strong>{assignment.employeeFullName}</strong>?
      </Typography>

      <form onSubmit={handleSubmit(onSubmit)}>
        <FormField
          label="Data zakończenia"
          name="assignedUntil"
          type="date"
          register={register}
          error={errors.assignedUntil}
          required
        />
      </form>
    </Modal>
  );
}
