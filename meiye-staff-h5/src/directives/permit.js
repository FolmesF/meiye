import { useUserStore } from '../stores/user'

function allowed(code) {
  if (!code) return true
  return useUserStore().hasPermission(code)
}

function apply(el, binding) {
  el.style.display = allowed(binding.value) ? '' : 'none'
}

export const permit = {
  mounted: apply,
  updated: apply
}
