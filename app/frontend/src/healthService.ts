import { api } from './api';

export interface HealthResponse {
  status: string;
  service: string;
  timestamp: string;
}

export async function getHealth(): Promise<HealthResponse> {
  const { data } = await api.get<HealthResponse>('/health');
  return data;
}
