import { BaseApi } from "../server"

export const getAllschedul = async (): Promise<any> => {
  const response = await BaseApi.get('/admin/v1/schedules');
  return response;
}

export const sendschedul = async (data: any): Promise<any> => {
  const response = await BaseApi.post('/admin/v1/schedules', data);
  return response;
}

export const updateScheduleByDay = async (day: string, data: any): Promise<any> => {
  const response = await BaseApi.put(`/admin/v1/schedules/${day}`, data)
  return response.data
}
