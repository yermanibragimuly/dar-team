import { BaseApi } from "../server"

export const getAllNews = async (page: number = 0, size: number = 9 ): Promise<any> =>{
  const response = await BaseApi.get('/admin/v1/news',{
    params: {
      page: page,
      size: size
    }
  });
  return response.data;
}

export const sendNews = async (data: any): Promise<any> => {
  const response = await BaseApi.post('/admin/v1/news', data);
  return response;
}

export const deleteNewsId = async (id: any): Promise<any> => {
  const response = await BaseApi.delete(`/admin/v1/news/${id}`);
  return response;
}

export const deleteNewsImgId = async (id: any): Promise<any> => {
  const response = await BaseApi.delete(`/admin/v1/news/${id}/image`);
  return response;
}

export const updateNewsId = async (id: any, data: any): Promise<any> => {
  const response = await BaseApi.put(`/admin/v1/news/${id}`,data);
  return response;
}

export const getSoloNewsId = async (id: any): Promise<any> => {
  const response = await BaseApi.get(`/admin/v1/news/${id}`);
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