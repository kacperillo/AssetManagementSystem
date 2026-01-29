import { useQuery } from '@tanstack/react-query';
import { Box, Typography, Chip } from '@mui/material';
import DataTable, { Column } from '../components/data/DataTable';
import ErrorMessage from '../components/feedback/ErrorMessage';
import { getMyAssignments, Assignment } from '../api/assignments';

const assetTypeLabels: Record<string, string> = {
  LAPTOP: 'Laptop',
  SMARTPHONE: 'Smartfon',
  TABLET: 'Tablet',
  PRINTER: 'Drukarka',
  HEADPHONES: 'Słuchawki',
};

const columns: Column<Assignment>[] = [
  { id: 'id', label: 'ID przydziału' },
  { id: 'assetId', label: 'ID zasobu' },
  {
    id: 'assetType',
    label: 'Typ',
    render: (row) => assetTypeLabels[row.assetType] || row.assetType,
  },
  { id: 'vendor', label: 'Producent' },
  { id: 'model', label: 'Model' },
  { id: 'seriesNumber', label: 'Numer seryjny' },
  { id: 'assignedFrom', label: 'Data od' },
  {
    id: 'assignedUntil',
    label: 'Data do',
    render: (row) => row.assignedUntil || '-',
  },
  {
    id: 'isActive',
    label: 'Status',
    render: (row) => (
      <Chip
        label={row.isActive ? 'Aktywny' : 'Zakończony'}
        color={row.isActive ? 'success' : 'default'}
        size="small"
      />
    ),
  },
];

export default function MyHistoryPage() {
  const {
    data: assignments = [],
    isLoading,
    error,
  } = useQuery({
    queryKey: ['my-history'],
    queryFn: getMyAssignments,
  });

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3 }}>
        Historia przydziałów
      </Typography>

      {error && <ErrorMessage message="Błąd podczas pobierania historii" />}

      <DataTable
        columns={columns}
        data={assignments}
        loading={isLoading}
        emptyMessage="Brak historii przydziałów"
      />
    </Box>
  );
}
