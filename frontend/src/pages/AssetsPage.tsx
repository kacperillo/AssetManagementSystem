import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Box, Button, Typography, Chip, Tooltip, IconButton } from '@mui/material';
import { Add, Block } from '@mui/icons-material';
import DataTable, { Column } from '../components/data/DataTable';
import Pagination from '../components/data/Pagination';
import AddAssetModal from '../components/modals/AddAssetModal';
import ConfirmDialog from '../components/forms/ConfirmDialog';
import ErrorMessage from '../components/feedback/ErrorMessage';
import { getAssets, deactivateAsset, Asset } from '../api/assets';

const assetTypeLabels: Record<string, string> = {
  LAPTOP: 'Laptop',
  SMARTPHONE: 'Smartfon',
  TABLET: 'Tablet',
  PRINTER: 'Drukarka',
  HEADPHONES: 'Słuchawki',
};

export default function AssetsPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [modalOpen, setModalOpen] = useState(false);
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [selectedAsset, setSelectedAsset] = useState<Asset | null>(null);

  const {
    data,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['assets', page, size],
    queryFn: () => getAssets(page, size),
  });

  const deactivateMutation = useMutation({
    mutationFn: (id: number) => deactivateAsset(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['assets'] });
      setConfirmDialogOpen(false);
      setSelectedAsset(null);
    },
  });

  const handleDeactivate = (asset: Asset) => {
    setSelectedAsset(asset);
    setConfirmDialogOpen(true);
  };

  const confirmDeactivate = () => {
    if (selectedAsset) {
      deactivateMutation.mutate(selectedAsset.id);
    }
  };

  const columns: Column<Asset>[] = [
    { id: 'id', label: 'ID' },
    {
      id: 'assetType',
      label: 'Typ',
      render: (row) => assetTypeLabels[row.assetType] || row.assetType,
    },
    { id: 'vendor', label: 'Producent' },
    { id: 'model', label: 'Model' },
    { id: 'seriesNumber', label: 'Numer seryjny' },
    {
      id: 'isActive',
      label: 'Status',
      render: (row) => (
        <Chip
          label={row.isActive ? 'Aktywny' : 'Nieaktywny'}
          color={row.isActive ? 'success' : 'default'}
          size="small"
        />
      ),
    },
    {
      id: 'assignedEmployee',
      label: 'Przypisany pracownik',
      render: (row) =>
        row.assignedEmployeeId
          ? `${row.assignedEmployeeFullName} (${row.assignedEmployeeEmail})`
          : '-',
    },
    {
      id: 'actions',
      label: 'Akcje',
      render: (row) => {
        const canDeactivate = row.isActive && !row.assignedEmployeeId;
        return (
          <Tooltip
            title={
              !row.isActive
                ? 'Zasób jest już nieaktywny'
                : row.assignedEmployeeId
                ? 'Nie można dezaktywować przypisanego zasobu'
                : 'Dezaktywuj zasób'
            }
          >
            <span>
              <IconButton
                size="small"
                color="warning"
                onClick={() => handleDeactivate(row)}
                disabled={!canDeactivate}
              >
                <Block />
              </IconButton>
            </span>
          </Tooltip>
        );
      },
    },
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Zasoby</Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => setModalOpen(true)}
        >
          Dodaj zasób
        </Button>
      </Box>

      {error && <ErrorMessage message="Błąd podczas pobierania zasobów" />}

      <DataTable
        columns={columns}
        data={data?.content || []}
        loading={isLoading}
        emptyMessage="Brak zasobów w systemie"
      />

      {data && data.totalElements > 0 && (
        <Pagination
          page={page}
          size={size}
          totalElements={data.totalElements}
          onPageChange={setPage}
          onSizeChange={(newSize) => {
            setSize(newSize);
            setPage(0);
          }}
        />
      )}

      <AddAssetModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onSuccess={() => queryClient.invalidateQueries({ queryKey: ['assets'] })}
      />

      <ConfirmDialog
        open={confirmDialogOpen}
        title="Dezaktywuj zasób"
        message={`Czy na pewno chcesz dezaktywować zasób ${selectedAsset?.vendor} ${selectedAsset?.model} (${selectedAsset?.seriesNumber})?`}
        onConfirm={confirmDeactivate}
        onCancel={() => {
          setConfirmDialogOpen(false);
          setSelectedAsset(null);
        }}
        confirmText="Dezaktywuj"
        loading={deactivateMutation.isPending}
      />
    </Box>
  );
}
