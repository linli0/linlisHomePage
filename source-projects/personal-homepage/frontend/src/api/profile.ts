import { get, post, put, del } from '@/utils/request'
import type { Profile, Skill, Project } from '@/types'

export function getProfile(): Promise<Profile> {
  return get('/profile')
}

export function getSkills(): Promise<Skill[]> {
  return get('/profile/skills')
}

export function getProjects(): Promise<Project[]> {
  return get('/profile/projects')
}

export function getFeaturedProjects(): Promise<Project[]> {
  return get('/profile/projects/featured')
}

export function createSkill(data: Partial<Skill>): Promise<Skill> {
  return post('/profile/skills', data)
}

export function updateSkill(id: number, data: Partial<Skill>): Promise<Skill> {
  return put(`/profile/skills/${id}`, data)
}

export function deleteSkill(id: number): Promise<void> {
  return del(`/profile/skills/${id}`)
}

export function createProject(data: Partial<Project>): Promise<Project> {
  return post('/profile/projects', data)
}

export function updateProject(id: number, data: Partial<Project>): Promise<Project> {
  return put(`/profile/projects/${id}`, data)
}

export function deleteProject(id: number): Promise<void> {
  return del(`/profile/projects/${id}`)
}
