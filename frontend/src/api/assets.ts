import apiClient from './client';

export type AssetType = 'LAPTOP' | 'SMARTPHONE' | 'TABLET' | 'PRINTER' | 'HEADPHONES';

export interface Asset {
  id: number;
  assetType: AssetType;
  vendor: string;
  model: string;
  seriesNumber: string;
  isActive: boolean;
  assignedEmployeeId: number | null;
  assignedEmployeeFullName: string | null;
  assignedEmployeeEmail: string | null;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface CreateAssetRequest {
  assetType: AssetType;
  vendor: string;
  model: string;
  seriesNumber: string;
}

export interface MyAsset {
  assetType: AssetType;
  vendor: string;
  model: string;
  seriesNumber: string;
  assignedFrom: string;
}

export const getAssets = async (
  page: number = 0,
  size: number = 10,
  sort: string = 'id,asc'
): Promise<PagedResponse<Asset>> => {
  const response = await apiClient.get<PagedResponse<Asset>>('/admin/assets', {
    params: { page, size, sort },
  });
  return response.data;
};

export const getAssetById = async (id: number): Promise<Asset> => {
  const response = await apiClient.get<Asset>(`/admin/assets/${id}`);
  return response.data;
};

export const createAsset = async (data: CreateAssetRequest): Promise<Asset> => {
  const response = await apiClient.post<Asset>('/admin/assets', data);
  return response.data;
};

export const deactivateAsset = async (id: number): Promise<Asset> => {
  const response = await apiClient.put<Asset>(`/admin/assets/${id}/deactivate`);
  return response.data;
};

export const getMyAssets = async (): Promise<MyAsset[]> => {
  const response = await apiClient.get<MyAsset[]>('/employee/assets');
  return response.data;
};
