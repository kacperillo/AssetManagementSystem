import apiClient from './client';
import { PagedResponse, AssetType } from './assets';

export interface Assignment {
  id: number;
  assetId: number;
  assetType: AssetType;
  vendor: string;
  model: string;
  seriesNumber: string;
  employeeId: number;
  employeeFullName: string;
  assignedFrom: string;
  assignedUntil: string | null;
  isActive: boolean;
}

export interface CreateAssignmentRequest {
  employeeId: number;
  assetId: number;
  assignedFrom: string;
}

export interface EndAssignmentRequest {
  assignedUntil: string;
}

export const getAssignments = async (
  page: number = 0,
  size: number = 10,
  sort: string = 'id,asc',
  employeeId?: number,
  assetId?: number
): Promise<PagedResponse<Assignment> | Assignment[]> => {
  const params: Record<string, unknown> = { page, size, sort };
  if (employeeId) params.employeeId = employeeId;
  if (assetId) params.assetId = assetId;

  const response = await apiClient.get('/admin/assignments', { params });
  return response.data;
};

export const createAssignment = async (data: CreateAssignmentRequest): Promise<Assignment> => {
  const response = await apiClient.post<Assignment>('/admin/assignments', data);
  return response.data;
};

export const endAssignment = async (id: number, data: EndAssignmentRequest): Promise<Assignment> => {
  const response = await apiClient.put<Assignment>(`/admin/assignments/${id}/end`, data);
  return response.data;
};

export const getMyAssignments = async (): Promise<Assignment[]> => {
  const response = await apiClient.get<Assignment[]>('/employee/assignments');
  return response.data;
};
