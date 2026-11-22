export default {
  plugins: {
    'postcss-pxtorem': {
      rootValue: 37.5,
      unitPrecision: 5,
      propList: ['*'],
      selectorBlackList: [],
      minPixelValue: 1,
      exclude: (file) => /node_modules/.test(file) && !/vant/.test(file)
    }
  }
}
