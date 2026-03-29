import request from '@/utils/request'

export interface LoginRequest {
  password: string
}

export interface AuthResponse {
  token: string
  type: string
  id: number
  username: string
  email: string
  displayName: string
  avatar: string
  role: string
  expiresIn: number
}

export const authApi = {
  login: (data: LoginRequest) => request.post('/auth/login', data),
  getCurrentUser: () => request.get('/auth/me')
}
