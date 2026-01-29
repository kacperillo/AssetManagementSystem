import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button, Alert } from '@mui/material';
import Modal from '../forms/Modal';
import FormField from '../forms/FormField';
import { createAsset, AssetType } from '../../api/assets';
import { AxiosError } from 'axios';

const schema = z.object({
  assetType: z.enum(['LAPTOP', 'SMARTPHONE', 'TABLET', 'PRINTER', 'HEADPHONES'], {
    required_error: 'Typ zasobu jest wymagany',
  }),
  vendor: z.string().min(1, 'Producent jest wymagany'),
  model: z.string().min(1, 'Model jest wymagany'),
  seriesNumber: z.string().min(1, 'Numer seryjny jest wymagany'),
});

type FormData = z.infer<typeof schema>;

interface AddAssetModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const assetTypeOptions = [
  { value: 'LAPTOP', label: 'Laptop' },
  { value: 'SMARTPHONE', label: 'Smartfon' },
  { value: 'TABLET', label: 'Tablet' },
  { value: 'PRINTER', label: 'Drukarka' },
  { value: 'HEADPHONES', label: 'Słuchawki' },
];

export default function AddAssetModal({ open, onClose, onSuccess }: AddAssetModalProps) {
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
    setLoading(true);
    setError(null);

    try {
      await createAsset({
        assetType: data.assetType as AssetType,
        vendor: data.vendor,
        model: data.model,
        seriesNumber: data.seriesNumber,
      });
      reset();
      onSuccess();
      onClose();
    } catch (err) {
      const axiosError = err as AxiosError<{ message: string }>;
      setError(axiosError.response?.data?.message || 'Wystąpił błąd podczas dodawania zasobu');
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
      title="Dodaj zasób"
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
          label="Typ zasobu"
          name="assetType"
          type="select"
          register={register}
          error={errors.assetType}
          required
          options={assetTypeOptions}
        />
        <FormField
          label="Producent"
          name="vendor"
          register={register}
          error={errors.vendor}
          required
        />
        <FormField
          label="Model"
          name="model"
          register={register}
          error={errors.model}
          required
        />
        <FormField
          label="Numer seryjny"
          name="seriesNumber"
          register={register}
          error={errors.seriesNumber}
          required
        />
      </form>
    </Modal>
  );
}
