import { showConfirmDialog, showDialog, showToast } from 'vant'

export function toast(message) {
  showToast(message || '请求失败')
}

export function dialog({ title = '提示', message, confirmButtonText = '知道了' } = {}) {
  return showDialog({ title, message, confirmButtonText })
}

export function confirm({ title = '提示', message, confirmButtonText = '确定', cancelButtonText = '取消' } = {}) {
  return showConfirmDialog({ title, message, confirmButtonText, cancelButtonText })
}
