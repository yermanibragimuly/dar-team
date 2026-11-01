import { BaseApi } from "../server"

export const getAllVideos = async (): Promise<any> => {
  const response = await BaseApi.get('/admin/v1/videos');
  return response;
}

export const getPublicAllVideos = async (): Promise<any> => {
  const response = await BaseApi.get('/api/v1/videos');
  return response;
}

export const postVideos = async (data:any): Promise<any> => {
  const response = await BaseApi.post('/admin/v1/videos', data);
  return response;
}

export const putIdVideos = async (id:any, data: any): Promise<any> => {
  const response = await BaseApi.put(`/admin/v1/videos/${id}`, data);
  return response;
}

export const deletIdVideos = async (id:any): Promise<any> => {
  const response = await BaseApi.delete(`/admin/v1/videos/${id}`);
  return response;
}

export const addSubscribeIdVideos = async (id:any, link: any): Promise<any> => {
  const response = await BaseApi.post(`/api/v1/subscriptions`, { videoId: id, videoLink: link });
  return response;
}