import { BaseApi } from "../server"

// Для витрины магазина (ShopView.vue)
export const getPublicProducts = async (): Promise<any> => {
  const response = await BaseApi.get('/api/v1/shop/products')
  return response
}

// Для админки (ShopAdmin.vue)
export const getAdminProducts = async (): Promise<any> => {
  const response = await BaseApi.get('/admin/v1/shop/products');
  return response;
}

export const createProduct = async (data: any): Promise<any> => {
  const response = await BaseApi.post('/admin/v1/shop/products', data);
  return response;
}

export const updateProduct = async (id: number | string, data: any): Promise<any> => {
  const response = await BaseApi.put(`/admin/v1/shop/products/${id}`, data);
  return response;
}

export const toggleProductStatus = async (id: number | string, active: boolean): Promise<any> => {
  const response = await BaseApi.patch(`/admin/v1/shop/products/${id}/status`, null, {
    params: { active }
  });
  return response;
}

export const deleteProduct = async (id: number | string): Promise<any> => {
  const response = await BaseApi.delete(`/admin/v1/shop/products/${id}`);
  return response;
}

export const sendImgMinio = async (file: File, nameFolder: string = 'shop') => {
  const formData = new FormData();
  formData.append("file", file);

  const response = await BaseApi.post(`/admin/v1/upload/${nameFolder}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    }
  });

  return response;
}
