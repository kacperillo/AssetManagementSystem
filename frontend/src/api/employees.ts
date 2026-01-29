import apiClient from './client';

export interface Employee {
  id: number;
  fullName: string;
  email: string;
  role: 'ADMIN' | 'EMPLOYEE';
  hiredFrom: string;
  hiredUntil: string | null;
}

export interface CreateEmployeeRequest {
  fullName: string;
  email: string;
  password: string;
  role: 'ADMIN' | 'EMPLOYEE';
  hiredFrom: string;
  hiredUntil?: string | null;
}

export const getEmployees = async (): Promise<Employee[]> => {
  const response = await apiClient.get<Employee[]>('/admin/employees');
  return response.data;
};

export const getEmployeeById = async (id: number): Promise<Employee> => {
  const response = await apiClient.get<Employee>(`/admin/employees/${id}`);
  return response.data;
};

export const createEmployee = async (data: CreateEmployeeRequest): Promise<Employee> => {
  const response = await apiClient.post<Employee>('/admin/employees', data);
  return response.data;
};
