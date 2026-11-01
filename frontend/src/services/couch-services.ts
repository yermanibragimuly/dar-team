import { BaseApi } from "../server"

export const getAllCouches = async (): Promise<any> => {
  const response = await BaseApi.get('/admin/v1/coaches');
  return response;
}

export const sendCouches = async (data: any): Promise<any> => {
  const response = await BaseApi.post('/admin/v1/coaches', data);
  return response;
}

export const deleteCoucheId = async (id: any): Promise<any> => {
  const response = await BaseApi.delete(`/admin/v1/coaches/${id}`);
  return response;
}

export const deleteMeritId = async (coachId: string, meritId: string): Promise<any> => {
  const response = await BaseApi.delete(`/admin/v1/coaches/${coachId}/merit/${meritId}`);
  return response;
}

export const deleteCoucheImgId = async (id: any): Promise<any> => {
  const response = await BaseApi.delete(`/admin/v1/coaches/${id}/image`);
  return response;
}

export const updateCoucheId = async (id: any, data: any): Promise<any> => {
  const response = await BaseApi.put(`/admin/v1/coaches/${id}`,data);
  return response;
}

export const getPersonCoucheId = async (id: any): Promise<any> => {
  const response = await BaseApi.get(`/admin/v1/coaches/${id}`);
  return response;
}

export const sendImgMinio = async (file: File, nameFolder: string) => {
  const formData = new FormData();
  formData.append("file", file);

  const response = await BaseApi.post(`/admin/v1/upload/${nameFolder}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    }
  });

  return response;
}