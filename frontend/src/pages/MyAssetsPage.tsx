import { useQuery } from '@tanstack/react-query';
import { Box, Typography } from '@mui/material';
import DataTable, { Column } from '../components/data/DataTable';
import ErrorMessage from '../components/feedback/ErrorMessage';
import { getMyAssets, MyAsset } from '../api/assets';

const assetTypeLabels: Record<string, string> = {
  LAPTOP: 'Laptop',
  SMARTPHONE: 'Smartfon',
  TABLET: 'Tablet',
  PRINTER: 'Drukarka',
  HEADPHONES: 'Słuchawki',
};

const columns: Column<MyAsset>[] = [
  {
    id: 'assetType',
    label: 'Typ',
    render: (row) => assetTypeLabels[row.assetType] || row.assetType,
  },
  { id: 'vendor', label: 'Producent' },
  { id: 'model', label: 'Model' },
  { id: 'seriesNumber', label: 'Numer seryjny' },
  { id: 'assignedFrom', label: 'Data przydziału' },
];

export default function MyAssetsPage() {
  const {
    data: assets = [],
    isLoading,
    error,
  } = useQuery({
    queryKey: ['my-assets'],
    queryFn: getMyAssets,
  });

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 3 }}>
        Moje zasoby
      </Typography>

      {error && <ErrorMessage message="Błąd podczas pobierania zasobów" />}

      <DataTable
        columns={columns}
        data={assets}
        loading={isLoading}
        emptyMessage="Nie masz przydzielonych zasobów"
        keyField="seriesNumber"
      />
    </Box>
  );
}
