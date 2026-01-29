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

export interface AssignmentFilters {
  employeeId?: number;
  assetId?: number;
  isActive?: boolean | null;
}

export const getAssignments = async (
  page: number = 0,
  size: number = 10,
  sort: string = 'id,asc',
  filters?: AssignmentFilters
): Promise<PagedResponse<Assignment>> => {
  const params: Record<string, unknown> = { page, size, sort };
  if (filters?.employeeId) params.employeeId = filters.employeeId;
  if (filters?.assetId) params.assetId = filters.assetId;
  if (filters?.isActive !== undefined && filters.isActive !== null) {
    params.isActive = filters.isActive;
  }

  const response = await apiClient.get<PagedResponse<Assignment>>('/admin/assignments', { params });
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
