const DESIGN_WIDTH = 375
const ROOT_VALUE = 37.5
const MAX_WIDTH = 540

export function setRem() {
  const width = Math.min(document.documentElement.clientWidth || DESIGN_WIDTH, MAX_WIDTH)
  document.documentElement.style.fontSize = `${(width / DESIGN_WIDTH) * ROOT_VALUE}px`
}

setRem()
window.addEventListener('resize', setRem)
window.addEventListener('pageshow', setRem)
