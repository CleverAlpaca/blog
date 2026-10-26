/**
 * 后端接口类型定义（与 blog-server 的 VO 一一对应）
 * 统一响应体由 utils/request.ts 解包，这里只描述 data 部分。
 */

/** 标签（TagVO；文章卡片内 count 为 null） */
export interface Tag {
  id: number
  name: string
  count?: number | null
}

/** 分类（CategoryVO） */
export interface Category {
  id: number
  name: string
  description?: string | null
  sort?: number
  articleCount?: number | null
}

/** 友链（FriendLinkVO） */
export interface FriendLink {
  id: number
  name: string
  logo?: string | null
  url: string
  description?: string | null
  sort?: number
}

/** 文章卡片（ArticleCardVO，列表/热门共用） */
export interface ArticleCard {
  id: number
  title: string
  summary?: string | null
  coverImage?: string | null
  status?: number
  isTop?: number
  categoryId?: number | null
  categoryName?: string | null
  tags?: Tag[] | null
  views?: number | null
  likes?: number | null
  commentCount?: number | null
  createTime?: string | null
}

/** 文章详情（ArticleDetailVO） */
export interface ArticleDetail extends ArticleCard {
  content?: string | null
  updateTime?: string | null
  prevId?: number | null
  prevTitle?: string | null
  nextId?: number | null
  nextTitle?: string | null
  liked?: boolean | null
}

/** 点赞开关返回（LikeVO） */
export interface LikeResult {
  liked: boolean
  likes: number
}

/** 归档文章项（ArchiveItemVO） */
export interface ArchiveItem {
  id: number
  title: string
  createTime?: string | null
}

/** 归档年份分组（ArchiveYearVO） */
export interface ArchiveYear {
  year: number
  articles: ArchiveItem[]
}

/** 两级评论/留言节点（CommentVO / MessageVO 结构一致） */
export interface CommentNode {
  id: number
  parentId?: number | null
  nickname: string
  content: string
  createTime?: string | null
  replies?: CommentNode[] | null
}

/** 发表评论/留言请求体（CommentPostRequest / MessagePostRequest） */
export interface CommentPost {
  parentId?: number
  nickname: string
  email?: string
  content: string
}

/** 关于页（AboutVO；content 为 Markdown，可能为 null） */
export interface About {
  nickname: string
  avatar?: string | null
  intro?: string | null
  email?: string | null
  content?: string | null
}

/** 站点统计（SiteStatsVO） */
export interface SiteStats {
  articleCount: number
  categoryCount: number
  tagCount: number
  commentCount: number
  runDays: number
}

/** 分页响应（PageResult） */
export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

/** 文章列表查询参数 */
export interface ArticleQuery {
  pageNum?: number
  pageSize?: number
  categoryId?: number
  tagId?: number
  keyword?: string
}

/** 文章目录项（从 Markdown 提取 h1~h3） */
export interface TocItem {
  id: string
  text: string
  level: 1 | 2 | 3
}
