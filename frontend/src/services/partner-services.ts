import { BaseApi } from "@/server"

export interface Partner {
  id?: number
  name: string
  logoUrl: string
  websiteUrl: string
  positionIndex?: number
}

export const getPublicPartners = async (): Promise<Partner[]> => {
  const res = await BaseApi.get('/api/v1/partners')
  return res.data || res || []
}

export const getAdminPartners = async (): Promise<Partner[]> => {
  const res = await BaseApi.get('/admin/v1/partners')
  return res.data || res || []
}

export const createPartner = async (data: Partner): Promise<Partner> => {
  const res = await BaseApi.post('/admin/v1/partners', data)
  return res.data
}

export const updatePartner = async (id: number, data: Partner): Promise<Partner> => {
  const res = await BaseApi.put(`/admin/v1/partners/${id}`, data)
  return res.data
}

export const deletePartner = async (id: number): Promise<void> => {
  await BaseApi.delete(`/admin/v1/partners/${id}`)
}

export const sendPartnerLogoMinio = async (file: File) => {
  const formData = new FormData()
  formData.append('file', file)

  const res = await BaseApi.post('/admin/v1/upload/partners', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return res.data
}