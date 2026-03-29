import request from '@/utils/request'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
  displayName: string
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
  register: (data: RegisterRequest) => request.post('/auth/register', data),
  getCurrentUser: () => request.get('/auth/me')
}
