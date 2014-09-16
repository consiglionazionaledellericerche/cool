// CNR.Data mock sample

CNR.Data.proxy.jsRemote = CNR.mock(CNR.Data.proxy.jsRemote, {
  // (optional) matching function
  matching: function (opts) {
    return opts.data.indexOf('hello') >= 0;
  },
  fn: function (opts) {
  	// opts contains CNR.Data options
    return {
      output: {
      	content: 'hello ' + CNR.User.id
      }
    };
  }
});