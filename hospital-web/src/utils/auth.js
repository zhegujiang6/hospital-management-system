const TOKEN_KEY = 'hospital_token'
const USER_KEY = 'hospital_user'

export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function getCurrentUser() {
  const value = sessionStorage.getItem(USER_KEY)
  if (!value) return null

  try {
    return JSON.parse(value)
  } catch {
    clearAuth()
    return null
  }
}

export function saveAuth(loginData) {
  const user = {
    userId: loginData.userId,
    username: loginData.username,
    realName: loginData.realName,
    role: loginData.role,
    doctorId: loginData.doctorId,
  }

  sessionStorage.setItem(TOKEN_KEY, loginData.token)
  sessionStorage.setItem(USER_KEY, JSON.stringify(user))

  // 清理旧版本保存在localStorage中的登录信息，避免留下过期Token
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)

  return user
}

export function clearAuth() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(USER_KEY)

  // 同时兼容并清理旧版本保存的登录信息
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}
