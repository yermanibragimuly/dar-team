import { BaseApi } from "../server"

export const getAllFighters = async (): Promise<any> => {
  const response = await BaseApi.get('/admin/v1/fighters');
  return response;
}

export const postUpdateTableAllFighters = async (ids: number[]): Promise<any> => {
  const response = await BaseApi.post('/admin/v1/fighters/reorder', ids);
  return response;
}

export const senFighters = async (data: any): Promise<any> => {
  const response = await BaseApi.post('/admin/v1/fighters', data);
  return response;
}

export const deleteFighterId = async (id: any): Promise<any> => {
  const response = await BaseApi.delete(`/admin/v1/fighters/${id}`);
  return response;
}

export const deleteFighterImgId = async (id: any): Promise<any> => {
  const response = await BaseApi.delete(`/admin/v1/fighters/${id}/image`);
  return response;
}

export const deleteAchievementId = async (fighterId: string, achievementsId: string): Promise<any> => {
  const response = await BaseApi.delete(`/admin/v1/fighters/${fighterId}/achievements/${achievementsId}`);
  return response;
}

export const updateFighterId = async (id: any, data: any): Promise<any> => {
  const response = await BaseApi.put(`/admin/v1/fighters/${id}`,data);
  return response;
}

export const getPersonFightersId = async (id: any): Promise<any> => {
  const response = await BaseApi.get(`/admin/v1/fighters/${id}`);
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
