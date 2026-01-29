import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Box, Button, Typography } from '@mui/material';
import { Add } from '@mui/icons-material';
import DataTable, { Column } from '../components/data/DataTable';
import AddEmployeeModal from '../components/modals/AddEmployeeModal';
import ErrorMessage from '../components/feedback/ErrorMessage';
import { getEmployees, Employee } from '../api/employees';

const columns: Column<Employee>[] = [
  { id: 'id', label: 'ID' },
  { id: 'fullName', label: 'Imię i nazwisko' },
  { id: 'email', label: 'Email' },
  {
    id: 'role',
    label: 'Rola',
    render: (row) => (row.role === 'ADMIN' ? 'Administrator' : 'Pracownik'),
  },
  { id: 'hiredFrom', label: 'Data zatrudnienia od' },
  {
    id: 'hiredUntil',
    label: 'Data zatrudnienia do',
    render: (row) => row.hiredUntil || '-',
  },
];

export default function EmployeesPage() {
  const [modalOpen, setModalOpen] = useState(false);

  const {
    data: employees = [],
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ['employees'],
    queryFn: getEmployees,
  });

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Pracownicy</Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => setModalOpen(true)}
        >
          Dodaj pracownika
        </Button>
      </Box>

      {error && <ErrorMessage message="Błąd podczas pobierania pracowników" />}

      <DataTable
        columns={columns}
        data={employees}
        loading={isLoading}
        emptyMessage="Brak pracowników w systemie"
      />

      <AddEmployeeModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onSuccess={() => refetch()}
      />
    </Box>
  );
}
