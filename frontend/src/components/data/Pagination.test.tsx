import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import Pagination from './Pagination';

describe('Pagination', () => {
  const defaultProps = {
    page: 0,
    size: 10,
    totalElements: 100,
    onPageChange: vi.fn(),
    onSizeChange: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render pagination component', () => {
    render(<Pagination {...defaultProps} />);

    expect(screen.getByText(/Wierszy na stronę:/i)).toBeInTheDocument();
  });

  it('should display correct range text', () => {
    render(<Pagination {...defaultProps} />);

    expect(screen.getByText('1-10 z 100')).toBeInTheDocument();
  });

  it('should display correct range for second page', () => {
    render(<Pagination {...defaultProps} page={1} />);

    expect(screen.getByText('11-20 z 100')).toBeInTheDocument();
  });

  it('should display correct range for last page with partial results', () => {
    render(<Pagination {...defaultProps} page={9} totalElements={95} />);

    expect(screen.getByText('91-95 z 95')).toBeInTheDocument();
  });

  it('should call onPageChange when clicking next page', () => {
    render(<Pagination {...defaultProps} />);

    const nextButton = screen.getByLabelText(/Go to next page/i);
    fireEvent.click(nextButton);

    expect(defaultProps.onPageChange).toHaveBeenCalledWith(1);
  });

  it('should call onPageChange when clicking previous page', () => {
    render(<Pagination {...defaultProps} page={1} />);

    const prevButton = screen.getByLabelText(/Go to previous page/i);
    fireEvent.click(prevButton);

    expect(defaultProps.onPageChange).toHaveBeenCalledWith(0);
  });

  it('should disable previous button on first page', () => {
    render(<Pagination {...defaultProps} page={0} />);

    const prevButton = screen.getByLabelText(/Go to previous page/i);
    expect(prevButton).toBeDisabled();
  });

  it('should disable next button on last page', () => {
    render(<Pagination {...defaultProps} page={9} />);

    const nextButton = screen.getByLabelText(/Go to next page/i);
    expect(nextButton).toBeDisabled();
  });

  it('should call onSizeChange when changing rows per page', () => {
    render(<Pagination {...defaultProps} />);

    const select = screen.getByRole('combobox');
    fireEvent.mouseDown(select);

    const option25 = screen.getByRole('option', { name: '25' });
    fireEvent.click(option25);

    expect(defaultProps.onSizeChange).toHaveBeenCalledWith(25);
  });

  it('should display available page size options', () => {
    render(<Pagination {...defaultProps} />);

    const select = screen.getByRole('combobox');
    fireEvent.mouseDown(select);

    expect(screen.getByRole('option', { name: '5' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: '10' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: '25' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: '50' })).toBeInTheDocument();
  });

  it('should show current page size as selected', () => {
    render(<Pagination {...defaultProps} size={25} />);

    const select = screen.getByRole('combobox');
    expect(select).toHaveTextContent('25');
  });

  it('should handle empty results', () => {
    render(<Pagination {...defaultProps} totalElements={0} />);

    expect(screen.getByText('0-0 z 0')).toBeInTheDocument();
  });
});
