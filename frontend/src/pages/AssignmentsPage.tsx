import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Button,
  Typography,
  Chip,
  IconButton,
  Tooltip,
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  Paper,
} from '@mui/material';
import { Add, Stop } from '@mui/icons-material';
import DataTable, { Column } from '../components/data/DataTable';
import Pagination from '../components/data/Pagination';
import FilterDropdown from '../components/data/FilterDropdown';
import CreateAssignmentModal from '../components/modals/CreateAssignmentModal';
import EndAssignmentModal from '../components/modals/EndAssignmentModal';
import ErrorMessage from '../components/feedback/ErrorMessage';
import { getAssignments, Assignment, AssignmentFilters } from '../api/assignments';
import { getEmployees } from '../api/employees';
import { getAssets, Asset } from '../api/assets';

const assetTypeLabels: Record<string, string> = {
  LAPTOP: 'Laptop',
  SMARTPHONE: 'Smartfon',
  TABLET: 'Tablet',
  PRINTER: 'Drukarka',
  HEADPHONES: 'Słuchawki',
};

type StatusFilter = 'all' | 'active' | 'ended';

export default function AssignmentsPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [employeeFilter, setEmployeeFilter] = useState<number | ''>('');
  const [assetFilter, setAssetFilter] = useState<number | ''>('');
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('all');
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [endModalOpen, setEndModalOpen] = useState(false);
  const [selectedAssignment, setSelectedAssignment] = useState<Assignment | null>(null);

  const filters: AssignmentFilters = {
    employeeId: employeeFilter || undefined,
    assetId: assetFilter || undefined,
    isActive: statusFilter === 'all' ? null : statusFilter === 'active',
  };

  const {
    data: assignmentsData,
    isLoading: assignmentsLoading,
    error: assignmentsError,
  } = useQuery({
    queryKey: ['assignments', page, size, employeeFilter, assetFilter, statusFilter],
    queryFn: () => getAssignments(page, size, 'id,asc', filters),
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

  const assignments = assignmentsData?.content || [];
  const totalElements = assignmentsData?.totalElements || 0;

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

      <Paper sx={{ p: 2, mb: 3 }}>
        <Box sx={{ display: 'flex', gap: 4, alignItems: 'flex-start', flexWrap: 'wrap' }}>
          <FormControl component="fieldset">
            <FormLabel component="legend">Status</FormLabel>
            <RadioGroup
              row
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value as StatusFilter);
                setPage(0);
              }}
            >
              <FormControlLabel value="all" control={<Radio size="small" />} label="Wszystkie" />
              <FormControlLabel value="active" control={<Radio size="small" />} label="Aktywne" />
              <FormControlLabel value="ended" control={<Radio size="small" />} label="Zakończone" />
            </RadioGroup>
          </FormControl>

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
      </Paper>

      {assignmentsError && <ErrorMessage message="Błąd podczas pobierania przydziałów" />}

      <DataTable
        columns={columns}
        data={assignments}
        loading={assignmentsLoading}
        emptyMessage="Brak przydziałów w systemie"
      />

      {totalElements > 0 && (
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
