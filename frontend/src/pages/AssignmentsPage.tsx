import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Box, Button, Typography, Chip, IconButton, Tooltip } from '@mui/material';
import { Add, Stop } from '@mui/icons-material';
import DataTable, { Column } from '../components/data/DataTable';
import Pagination from '../components/data/Pagination';
import FilterDropdown from '../components/data/FilterDropdown';
import CreateAssignmentModal from '../components/modals/CreateAssignmentModal';
import EndAssignmentModal from '../components/modals/EndAssignmentModal';
import ErrorMessage from '../components/feedback/ErrorMessage';
import { getAssignments, Assignment } from '../api/assignments';
import { getEmployees } from '../api/employees';
import { getAssets, PagedResponse, Asset } from '../api/assets';

const assetTypeLabels: Record<string, string> = {
  LAPTOP: 'Laptop',
  SMARTPHONE: 'Smartfon',
  TABLET: 'Tablet',
  PRINTER: 'Drukarka',
  HEADPHONES: 'Słuchawki',
};

export default function AssignmentsPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [employeeFilter, setEmployeeFilter] = useState<number | ''>('');
  const [assetFilter, setAssetFilter] = useState<number | ''>('');
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [endModalOpen, setEndModalOpen] = useState(false);
  const [selectedAssignment, setSelectedAssignment] = useState<Assignment | null>(null);

  const hasFilters = employeeFilter !== '' || assetFilter !== '';

  const {
    data: assignmentsData,
    isLoading: assignmentsLoading,
    error: assignmentsError,
  } = useQuery({
    queryKey: ['assignments', page, size, employeeFilter, assetFilter],
    queryFn: () =>
      getAssignments(
        page,
        size,
        'id,asc',
        employeeFilter || undefined,
        assetFilter || undefined
      ),
  });

  const { data: employees = [] } = useQuery({
    queryKey: ['employees'],
    queryFn: getEmployees,
  });

  const { data: assetsData } = useQuery({
    queryKey: ['assets-all'],
    queryFn: () => getAssets(0, 1000),
  });

  const assets = assetsData?.content || [];

  // Określ dane do wyświetlenia (z filtrami - tablica, bez - paginowane)
  const assignments: Assignment[] = hasFilters
    ? (assignmentsData as Assignment[]) || []
    : (assignmentsData as PagedResponse<Assignment>)?.content || [];

  const totalElements = hasFilters
    ? assignments.length
    : (assignmentsData as PagedResponse<Assignment>)?.totalElements || 0;

  const handleEndAssignment = (assignment: Assignment) => {
    setSelectedAssignment(assignment);
    setEndModalOpen(true);
  };

  const columns: Column<Assignment>[] = [
    { id: 'id', label: 'ID' },
    {
      id: 'assetType',
      label: 'Typ zasobu',
      render: (row) => assetTypeLabels[row.assetType] || row.assetType,
    },
    { id: 'vendor', label: 'Producent' },
    { id: 'model', label: 'Model' },
    { id: 'seriesNumber', label: 'Numer seryjny' },
    {
      id: 'employee',
      label: 'Pracownik',
      render: (row) => `${row.employeeFullName} (ID: ${row.employeeId})`,
    },
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
    {
      id: 'actions',
      label: 'Akcje',
      render: (row) => (
        <Tooltip title={row.isActive ? 'Zakończ przydział' : 'Przydział jest już zakończony'}>
          <span>
            <IconButton
              size="small"
              color="warning"
              onClick={() => handleEndAssignment(row)}
              disabled={!row.isActive}
            >
              <Stop />
            </IconButton>
          </span>
        </Tooltip>
      ),
    },
  ];

  const employeeOptions = employees.map((e) => ({
    value: e.id,
    label: `${e.fullName} (${e.email})`,
  }));

  const assetOptions = assets.map((a: Asset) => ({
    value: a.id,
    label: `${a.assetType} - ${a.vendor} ${a.model}`,
  }));

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Przydziały</Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => setCreateModalOpen(true)}
        >
          Utwórz przydział
        </Button>
      </Box>

      <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
        <FilterDropdown
          label="Filtruj po pracowniku"
          options={employeeOptions}
          value={employeeFilter}
          onChange={(value) => {
            setEmployeeFilter(value);
            setPage(0);
          }}
        />
        <FilterDropdown
          label="Filtruj po zasobie"
          options={assetOptions}
          value={assetFilter}
          onChange={(value) => {
            setAssetFilter(value);
            setPage(0);
          }}
        />
      </Box>

      {assignmentsError && <ErrorMessage message="Błąd podczas pobierania przydziałów" />}

      <DataTable
        columns={columns}
        data={assignments}
        loading={assignmentsLoading}
        emptyMessage="Brak przydziałów w systemie"
      />

      {!hasFilters && totalElements > 0 && (
        <Pagination
          page={page}
          size={size}
          totalElements={totalElements}
          onPageChange={setPage}
          onSizeChange={(newSize) => {
            setSize(newSize);
            setPage(0);
          }}
        />
      )}

      <CreateAssignmentModal
        open={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        onSuccess={() => {
          queryClient.invalidateQueries({ queryKey: ['assignments'] });
          queryClient.invalidateQueries({ queryKey: ['assets'] });
          queryClient.invalidateQueries({ queryKey: ['assets-all'] });
        }}
        employees={employees}
        assets={assets}
      />

      <EndAssignmentModal
        open={endModalOpen}
        onClose={() => {
          setEndModalOpen(false);
          setSelectedAssignment(null);
        }}
        onSuccess={() => {
          queryClient.invalidateQueries({ queryKey: ['assignments'] });
          queryClient.invalidateQueries({ queryKey: ['assets'] });
          queryClient.invalidateQueries({ queryKey: ['assets-all'] });
        }}
        assignment={selectedAssignment}
      />
    </Box>
  );
}
