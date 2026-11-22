export default {
  plugins: {
    'postcss-px-to-viewport-8-plugin': {
      viewportWidth: 375,
      unitPrecision: 5,
      propList: ['*'],
      selectorBlackList: ['.ignore'],
      minPixelValue: 1,
      mediaQuery: false,
      exclude: [/node_modules/]
    }
  }
}
