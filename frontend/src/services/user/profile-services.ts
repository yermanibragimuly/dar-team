import { BaseApi } from "../../server"

export const getDataProfile = async (): Promise<any> => {
  const response = await BaseApi.get('/user/profile');
  return response;
}

export const sendDataProfile = async (data: any): Promise<any> => {
  let sendData = {
    firstName: data.firstName,
    lastName: data.lastName,
    phone: data.phone,
    bio: data.bio,
    avatarUrl: null,
    birthDate: data.birthDate
  }
  const response = await BaseApi.put('/user/profile', sendData);
  return response;
}

export const getDataMyVideo = async (): Promise<any> => {
  const response = await BaseApi.get('api/v1/subscriptions/my');
  return response;
}