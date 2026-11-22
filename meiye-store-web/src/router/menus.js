import {
  Avatar,
  Box,
  Coin,
  DataLine,
  Goods,
  Menu,
  Present,
  ShoppingBag,
  User
} from '@element-plus/icons-vue'

export const iconMap = {
  Avatar,
  Box,
  Coin,
  DataLine,
  Goods,
  Menu,
  Present,
  ShoppingBag,
  User
}

export function menuIcon(name) {
  return iconMap[name] || Menu
}
