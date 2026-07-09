import { get, post, del } from '@/utils/request'
import type { FileInfo } from '@/types'

export function listFiles(path?: string): Promise<FileInfo[]> {
  return get('/files/list', { path })
}

export function uploadFile(file: File, directory?: string): Promise<FileInfo> {
  const formData = new FormData()
  formData.append('file', file)
  if (directory) {
    formData.append('directory', directory)
  }
  return post('/files/upload', formData)
}

export function downloadFile(id: number): string {
  return `/api/files/download/${id}`
}

export function downloadByToken(token: string): string {
  return `/api/files/share/${token}`
}

export function createShareLink(id: number, expireHours?: number): Promise<FileInfo> {
  return post(`/files/${id}/share`, { expireHours })
}

export function deleteFile(id: number): Promise<void> {
  return del(`/files/${id}`)
}
