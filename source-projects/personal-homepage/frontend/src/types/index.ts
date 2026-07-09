export interface Result<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageResult<T> {
  content: T[]
  pageNumber: number
  pageSize: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  hasNext: boolean
  hasPrevious: boolean
}

export interface Article {
  id: number
  title: string
  summary: string
  content: string
  coverImage: string
  viewCount: number
  isPublished: boolean
  isTop: boolean
  tags: Tag[]
  category: Category
  commentCount: number
  createdAt: string
  updatedAt: string
}

export interface Category {
  id: number
  name: string
  description: string
  sortOrder: number
  articleCount: number
  createdAt: string
}

export interface Tag {
  id: number
  name: string
  color: string
  articleCount: number
  createdAt: string
}

export interface Comment {
  id: number
  author: string
  email: string
  content: string
  articleId: number
  articleTitle: string
  parentId: number
  isApproved: boolean
  createdAt: string
  replies: Comment[]
}

export interface FileInfo {
  id: number
  name: string
  path: string
  url: string
  shareToken: string
  shareUrl: string
  fileSize: number
  fileSizeFormatted: string
  mimeType: string
  extension: string
  isDirectory: boolean
  isPublic: boolean
  downloadCount: number
  createdAt: string
  expiresAt: string
}

export interface Profile {
  name: string
  title: string
  bio: string
  email: string
  github: string
  linkedin: string
  website: string
  avatar: string
  skills: Skill[]
  skillsByCategory: Record<string, Skill[]>
  projects: Project[]
  featuredProjects: Project[]
}

export interface Skill {
  id: number
  name: string
  category: string
  proficiencyLevel: number
  icon: string
}

export interface Project {
  id: number
  name: string
  description: string
  url: string
  githubUrl: string
  image: string
  category: string
  technologies: string[]
  startDate: string
  endDate: string
  isFeatured: boolean
}

export interface Timeline {
  id: number
  title: string
  description: string
  type: string
  startDate: string
  endDate: string
  organization: string
  location: string
}

export interface CommandRequest {
  prompt: string
  context?: string
  model?: string
  stream?: boolean
}

export interface CommandResponse {
  historyId: number
  output: string
  exitCode: number
  success: boolean
  executionTimeMs: number
  executedAt: string
}

export interface CommandHistory {
  id: number
  commandType: string
  commandInput: string
  commandOutput: string
  exitCode: number
  executionTimeMs: number
  executedBy: string
  ipAddress: string
  isSuccess: boolean
  createdAt: string
}

export interface TimestampResult {
  timestampMillis: number
  timestampSeconds: number
  localTime: string
  utcTime: string
  isoTime: string
}
