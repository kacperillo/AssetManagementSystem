import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Button,
  Typography,
  Chip,
  Tooltip,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  RadioGroup,
  FormControlLabel,
  Radio,
  FormLabel,
  Paper,
} from '@mui/material';
import { Add, Block } from '@mui/icons-material';
import DataTable, { Column } from '../components/data/DataTable';
import Pagination from '../components/data/Pagination';
import AddAssetModal from '../components/modals/AddAssetModal';
import ConfirmDialog from '../components/forms/ConfirmDialog';
import ErrorMessage from '../components/feedback/ErrorMessage';
import { getAssets, deactivateAsset, Asset, AssetType, AssetFilters } from '../api/assets';

const assetTypeLabels: Record<string, string> = {
  LAPTOP: 'Laptop',
  SMARTPHONE: 'Smartfon',
  TABLET: 'Tablet',
  PRINTER: 'Drukarka',
  HEADPHONES: 'Słuchawki',
};

type StatusFilter = 'all' | 'active' | 'inactive';
type AssignedFilter = 'all' | 'assigned' | 'unassigned';

export default function AssetsPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [modalOpen, setModalOpen] = useState(false);
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [selectedAsset, setSelectedAsset] = useState<Asset | null>(null);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('all');
  const [assetTypeFilter, setAssetTypeFilter] = useState<AssetType | ''>('');
  const [assignedFilter, setAssignedFilter] = useState<AssignedFilter>('all');

  const filters: AssetFilters = {
    isActive: statusFilter === 'all' ? null : statusFilter === 'active',
    assetType: assetTypeFilter || null,
    isAssigned: assignedFilter === 'all' ? null : assignedFilter === 'assigned',
  };

  const {
    data,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['assets', page, size, statusFilter, assetTypeFilter, assignedFilter],
    queryFn: () => getAssets(page, size, 'id,asc', filters),
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

  const handleStatusFilterChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setStatusFilter(event.target.value as StatusFilter);
    setPage(0);
  };

  const handleAssetTypeFilterChange = (value: AssetType | '') => {
    setAssetTypeFilter(value);
    setPage(0);
  };

  const handleAssignedFilterChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setAssignedFilter(event.target.value as AssignedFilter);
    setPage(0);
  };

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

      <Paper sx={{ p: 2, mb: 3 }}>
        <Box sx={{ display: 'flex', gap: 4, alignItems: 'flex-start', flexWrap: 'wrap' }}>
          <FormControl component="fieldset">
            <FormLabel component="legend">Status</FormLabel>
            <RadioGroup
              row
              value={statusFilter}
              onChange={handleStatusFilterChange}
            >
              <FormControlLabel value="all" control={<Radio size="small" />} label="Wszystkie" />
              <FormControlLabel value="active" control={<Radio size="small" />} label="Aktywne" />
              <FormControlLabel value="inactive" control={<Radio size="small" />} label="Nieaktywne" />
            </RadioGroup>
          </FormControl>

          <FormControl component="fieldset">
            <FormLabel component="legend">Przypisanie</FormLabel>
            <RadioGroup
              row
              value={assignedFilter}
              onChange={handleAssignedFilterChange}
            >
              <FormControlLabel value="all" control={<Radio size="small" />} label="Wszystkie" />
              <FormControlLabel value="assigned" control={<Radio size="small" />} label="Przypisane" />
              <FormControlLabel value="unassigned" control={<Radio size="small" />} label="Nieprzypisane" />
            </RadioGroup>
          </FormControl>

          <FormControl sx={{ minWidth: 200 }} size="small">
            <InputLabel>Typ zasobu</InputLabel>
            <Select
              value={assetTypeFilter}
              label="Typ zasobu"
              onChange={(e) => handleAssetTypeFilterChange(e.target.value as AssetType | '')}
            >
              <MenuItem value="">Wszystkie typy</MenuItem>
              <MenuItem value="LAPTOP">Laptop</MenuItem>
              <MenuItem value="SMARTPHONE">Smartfon</MenuItem>
              <MenuItem value="TABLET">Tablet</MenuItem>
              <MenuItem value="PRINTER">Drukarka</MenuItem>
              <MenuItem value="HEADPHONES">Słuchawki</MenuItem>
            </Select>
          </FormControl>
        </Box>
      </Paper>

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
