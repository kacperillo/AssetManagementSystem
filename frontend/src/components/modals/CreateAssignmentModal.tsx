import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button, Alert, TextField, MenuItem } from '@mui/material';
import Modal from '../forms/Modal';
import { createAssignment } from '../../api/assignments';
import { Employee } from '../../api/employees';
import { Asset } from '../../api/assets';
import { AxiosError } from 'axios';

const getTodayDate = () => new Date().toISOString().split('T')[0];

const schema = z.object({
  employeeId: z.string().min(1, 'Pracownik jest wymagany'),
  assetId: z.string().min(1, 'Zasób jest wymagany'),
  assignedFrom: z.string()
    .min(1, 'Data rozpoczęcia jest wymagana')
    .refine((date) => date <= getTodayDate(), {
      message: 'Data nie może być w przyszłości',
    }),
});

type FormData = z.infer<typeof schema>;

interface CreateAssignmentModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
  employees: Employee[];
  assets: Asset[];
}

export default function CreateAssignmentModal({
  open,
  onClose,
  onSuccess,
  employees,
  assets,
}: CreateAssignmentModalProps) {
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const today = getTodayDate();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      assignedFrom: today,
    },
  });

  // Filtruj tylko dostępne zasoby (aktywne i nieprzypisane)
  const availableAssets = assets.filter(
    (asset) => asset.isActive && !asset.assignedEmployeeId
  );

  const onSubmit = async (data: FormData) => {
    setLoading(true);
    setError(null);

    try {
      await createAssignment({
        employeeId: parseInt(data.employeeId),
        assetId: parseInt(data.assetId),
        assignedFrom: data.assignedFrom,
      });
      reset();
      onSuccess();
      onClose();
    } catch (err) {
      const axiosError = err as AxiosError<{ message: string }>;
      setError(axiosError.response?.data?.message || 'Wystąpił błąd podczas tworzenia przydziału');
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
      title="Utwórz przydział"
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
            {loading ? 'Zapisywanie...' : 'Utwórz'}
          </Button>
        </>
      }
    >
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <form onSubmit={handleSubmit(onSubmit)}>
        <TextField
          select
          label="Pracownik"
          {...register('employeeId')}
          error={!!errors.employeeId}
          helperText={errors.employeeId?.message}
          fullWidth
          margin="normal"
          required
        >
          {employees.map((employee) => (
            <MenuItem key={employee.id} value={String(employee.id)}>
              {employee.fullName} ({employee.email})
            </MenuItem>
          ))}
        </TextField>

        <TextField
          select
          label="Zasób"
          {...register('assetId')}
          error={!!errors.assetId}
          helperText={errors.assetId?.message || (availableAssets.length === 0 ? 'Brak dostępnych zasobów' : '')}
          fullWidth
          margin="normal"
          required
          disabled={availableAssets.length === 0}
        >
          {availableAssets.map((asset) => (
            <MenuItem key={asset.id} value={String(asset.id)}>
              {asset.assetType} - {asset.vendor} {asset.model} ({asset.seriesNumber})
            </MenuItem>
          ))}
        </TextField>

        <TextField
          label="Data rozpoczęcia"
          type="date"
          {...register('assignedFrom')}
          error={!!errors.assignedFrom}
          helperText={errors.assignedFrom?.message}
          fullWidth
          margin="normal"
          required
          slotProps={{
            inputLabel: { shrink: true },
            htmlInput: { max: today },
          }}
        />
      </form>
    </Modal>
  );
}
