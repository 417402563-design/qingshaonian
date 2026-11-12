const TOKEN_KEY = 'qs_token'
const USER_KEY = 'qs_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setAuth(token, user) {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify(user || {}))
}

export function getUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || '{}')
  } catch {
    return {}
  }
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getRole() {
  return getUser().role || ''
}

export function homeByRole(role) {
  if (role === 'ADMIN') return '/admin/dashboard'
  if (role === 'COUNSELOR') return '/counselor/workbench'
  return '/student/home'
}
