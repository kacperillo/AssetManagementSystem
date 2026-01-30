import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import DataTable, { Column } from './DataTable';

interface TestData {
  id: number;
  name: string;
  email: string;
}

describe('DataTable', () => {
  const columns: Column<TestData>[] = [
    { id: 'id', label: 'ID' },
    { id: 'name', label: 'Nazwa' },
    { id: 'email', label: 'Email' },
  ];

  const testData: TestData[] = [
    { id: 1, name: 'Jan Kowalski', email: 'jan@example.com' },
    { id: 2, name: 'Anna Nowak', email: 'anna@example.com' },
  ];

  describe('Rendering Data', () => {
    it('should render table with column headers', () => {
      render(<DataTable columns={columns} data={testData} />);

      expect(screen.getByText('ID')).toBeInTheDocument();
      expect(screen.getByText('Nazwa')).toBeInTheDocument();
      expect(screen.getByText('Email')).toBeInTheDocument();
    });

    it('should render table with data rows', () => {
      render(<DataTable columns={columns} data={testData} />);

      expect(screen.getByText('Jan Kowalski')).toBeInTheDocument();
      expect(screen.getByText('jan@example.com')).toBeInTheDocument();
      expect(screen.getByText('Anna Nowak')).toBeInTheDocument();
      expect(screen.getByText('anna@example.com')).toBeInTheDocument();
    });

    it('should render correct number of rows', () => {
      render(<DataTable columns={columns} data={testData} />);

      const rows = screen.getAllByRole('row');
      // 1 header row + 2 data rows
      expect(rows).toHaveLength(3);
    });
  });

  describe('Loading State', () => {
    it('should show loading spinner when loading is true', () => {
      render(<DataTable columns={columns} data={[]} loading={true} />);

      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('should not show data table when loading', () => {
      render(<DataTable columns={columns} data={testData} loading={true} />);

      expect(screen.queryByRole('table')).not.toBeInTheDocument();
    });
  });

  describe('Empty State', () => {
    it('should show empty state when data is empty', () => {
      render(<DataTable columns={columns} data={[]} />);

      expect(screen.getByText('Brak danych')).toBeInTheDocument();
    });

    it('should show custom empty message', () => {
      render(
        <DataTable
          columns={columns}
          data={[]}
          emptyMessage="Nie znaleziono wyników"
        />
      );

      expect(screen.getByText('Nie znaleziono wyników')).toBeInTheDocument();
    });

    it('should not show table when data is empty', () => {
      render(<DataTable columns={columns} data={[]} />);

      expect(screen.queryByRole('table')).not.toBeInTheDocument();
    });
  });

  describe('Custom Rendering', () => {
    it('should use custom render function for column', () => {
      const columnsWithCustomRender: Column<TestData>[] = [
        { id: 'id', label: 'ID' },
        {
          id: 'name',
          label: 'Nazwa',
          render: (row) => <strong data-testid="custom-name">{row.name.toUpperCase()}</strong>,
        },
      ];

      render(<DataTable columns={columnsWithCustomRender} data={testData} />);

      const customNames = screen.getAllByTestId('custom-name');
      expect(customNames[0]).toHaveTextContent('JAN KOWALSKI');
      expect(customNames[1]).toHaveTextContent('ANNA NOWAK');
    });
  });

  describe('Key Field', () => {
    it('should use keyField for row keys when provided', () => {
      const { container } = render(
        <DataTable columns={columns} data={testData} keyField="id" />
      );

      const table = container.querySelector('table');
      expect(table).toBeInTheDocument();
    });
  });

  describe('Null/Undefined Values', () => {
    it('should display dash for null values', () => {
      const dataWithNull = [
        { id: 1, name: 'Test', email: null as unknown as string },
      ];

      render(<DataTable columns={columns} data={dataWithNull} />);

      expect(screen.getByText('-')).toBeInTheDocument();
    });

    it('should display dash for undefined values', () => {
      const dataWithUndefined = [
        { id: 1, name: 'Test', email: undefined as unknown as string },
      ];

      render(<DataTable columns={columns} data={dataWithUndefined} />);

      expect(screen.getByText('-')).toBeInTheDocument();
    });
  });

  describe('Multiple Columns', () => {
    it('should render correct number of columns', () => {
      render(<DataTable columns={columns} data={testData} />);

      const headerCells = screen.getAllByRole('columnheader');
      expect(headerCells).toHaveLength(3);
    });
  });
});
