/** ===== 通用 ===== */

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

/** ===== 登录 / 用户 ===== */

export interface LoginParams {
  username: string
  password: string
}

export interface UserVO {
  id: number
  username: string
  nickname: string
  avatar: string
  intro: string
  email: string
}

export interface LoginResult {
  token: string
  user: UserVO
}

export interface ProfileUpdateParams {
  nickname?: string
  avatar?: string
  email?: string
  intro?: string
}

export interface PasswordUpdateParams {
  oldPassword: string
  newPassword: string
}

/** ===== 文章 ===== */

export interface TagItem {
  id: number
  name: string
  /** 关联已发布文章数（文章卡片内为 null） */
  count?: number | null
}

export interface ArticleCard {
  id: number
  title: string
  summary: string
  coverImage: string
  /** 0 草稿 / 1 已发布 / 2 已下架 */
  status: number
  isTop: number
  categoryId: number | null
  categoryName: string
  tags: TagItem[]
  views: number
  likes: number
  commentCount: number
  createTime: string
}

export interface ArticleDetail extends ArticleCard {
  content: string
  updateTime: string
  prevId: number | null
  prevTitle: string
  nextId: number | null
  nextTitle: string
  liked: boolean | null
}

export interface ArticlePageParams {
  pageNum: number
  pageSize: number
  keyword?: string
  status?: number
  categoryId?: number
}

export interface ArticleSaveParams {
  title: string
  summary?: string
  coverImage?: string
  categoryId?: number
  tagIds: number[]
  content: string
  /** 0 草稿 / 1 已发布 / 2 已下架 */
  status: number
  /** 1 置顶 / 0 不置顶 */
  isTop: number
}

/** ===== 分类 ===== */

export interface CategoryItem {
  id: number
  name: string
  description: string
  sort: number
  articleCount: number
}

export interface CategorySaveParams {
  name: string
  description?: string
  sort?: number
}

/** ===== 评论 / 留言 ===== */

export interface AdminComment {
  id: number
  /** 留言时为 null */
  articleId: number | null
  /** 留言时为 null */
  articleTitle: string | null
  nickname: string
  content: string
  /** 1 显示 / 0 隐藏 */
  status: number
  createTime: string
}

export interface CommentPageParams {
  pageNum: number
  pageSize: number
  articleTitle?: string
  nickname?: string
}

export interface MessagePageParams {
  pageNum: number
  pageSize: number
  nickname?: string
}

/** ===== 友链 ===== */

export interface FriendLinkItem {
  id: number
  name: string
  logo: string
  url: string
  description: string
  sort: number
  /** 1 显示 / 0 隐藏 */
  status: number
}

export interface FriendLinkSaveParams {
  name: string
  logo?: string
  url: string
  description?: string
  sort?: number
  /** 1 显示 / 0 隐藏 */
  status?: number
}

/** ===== 看板 ===== */

export interface DashboardData {
  publishedCount: number
  draftCount: number
  categoryCount: number
  tagCount: number
  commentCount: number
  messageCount: number
  friendLinkCount: number
  totalViews: number
  totalLikes: number
  latestComments: AdminComment[]
}
