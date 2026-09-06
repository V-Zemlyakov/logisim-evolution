module.exports = {
  multipass: true,
  plugins: [
    {
      name: 'preset-default',
      params: {
        overrides: {
          convertPathData: {
            makeArcs: false
          },
          convertShapeToPath: false,
          convertTransform: true,
          collapseGroups: true,
          removeUnknownsAndDefaults: true
        }
      }
    }
  ]
};
