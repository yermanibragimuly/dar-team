import { BaseApi } from "../server"

export const auth = async (name: string, password: string): Promise<any> => {
  const response = await BaseApi.post('/auth/login',{
    username: name,
    password: password
  })
  return response.data
}

export const registerUser = async (name: string, password: string, email: string) => {
  const response = await BaseApi.post('/auth/register',{
    username: name,
    password: password,
    email: email
  })
  return response.data
}

export const sendOnEmailForgotPasswordUser = async (email: string) => {
  const response = await BaseApi.post('/auth/forgot-password',{
    loginOrEmail: email
  })
  return response.data
}

export const sendOnEmailResetPasswordUser = async (all: any) => {
  const response = await BaseApi.post('/auth/reset-password',{
    email: all.email,
    code: all.code,
    newPassword: all.password
  })
  return response.data
}

export const getconfirmUser = async (token: any) => {
  const response = await BaseApi.get('/auth/confirm',{
    params: {
      token: token
    }
  })
  return response.data
}