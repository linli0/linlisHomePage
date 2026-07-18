export interface TweetDTO {
  id: number
  tweetId: string
  content: string
  platform: 'twitter' | 'truth-social'
  username: string
  displayName?: string
  createdAt: string
  likesCount: number
  retweetsCount: number
  repliesCount: number
  hashtags: string[]
  mentions: string[]
  mediaUrls: string[]
}

export interface TweetSearchRequest {
  keyword?: string
  platform?: string
  username?: string
  startDate?: string
  endDate?: string
  limit?: number
}

export interface AccountStats {
  username: string
  displayName?: string
  platform: string
  tweetCount: number
  averageLikes: number
}

export interface TweetStatsDTO {
  totalTweets: number
  twitterTweets: number
  truthSocialTweets: number
  tweetsToday: number
  tweetsThisWeek: number
  topAccounts: AccountStats[]
  averageEngagement: number
}
