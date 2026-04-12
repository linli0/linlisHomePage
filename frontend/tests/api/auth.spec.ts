import { describe, test, expect } from 'vitest'
import { authApi } from '@/api/auth'
import type { LoginRequest } from '@/api/auth'

describe('Auth API', () => {
  test('should login successfully with valid password', async () => {
    const loginRequest: LoginRequest = {
      username: 'admin',
      password: 'admin123'
    }
    
    const response = await authApi.login(loginRequest)
    
    expect(response).toBeDefined()
    expect(response.token).toBe('mock-jwt-token-for-testing')
    expect(response.username).toBe('admin')
    expect(response.role).toBe('ADMIN')
  })

  test('should get current user', async () => {
    const response = await authApi.getCurrentUser()
    
    expect(response).toBeDefined()
    expect(response.username).toBe('testuser')
    expect(response.email).toBe('test@example.com')
  })

  test('should update profile', async () => {
    const updateData = {
      displayName: 'Updated Name',
      email: 'updated@example.com'
    }
    
    const response = await authApi.updateProfile(updateData)
    
    expect(response).toBeDefined()
  })

  test('should change password', async () => {
    const passwordData = {
      currentPassword: 'admin123',
      newPassword: 'newpassword123'
    }
    
    await authApi.changePassword(passwordData)
    // Should not throw error
  })
})