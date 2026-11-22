import { useUserStore } from '../stores/user'

function allowed(code) {
  if (!code || code === 'login') return Boolean(useUserStore().token)
  return true
}

function apply(el, binding) {
  el.style.display = allowed(binding.value) ? '' : 'none'
}

export const permit = {
  mounted: apply,
  updated: apply
}
