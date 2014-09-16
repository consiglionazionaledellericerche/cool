require(['json!common', 'modernizr'], function (common, Modernizr) {
  "use strict";

  window._gaq = window._gaq || [];
  window._gaq.push(['_setAccount', 'UA-41479790-1']); // testselezioni: UA-41492910-1
  window._gaq.push(['_trackPageview']);

  // CNR custom variables
  window._gaq.push(['_setCustomVar', 1, 'artifact_version', common.version, 1]);
  if (!common.User.isGuest) {
    window._gaq.push(['_setCustomVar', 2, 'username', common.User.id, 1]);
  }

  var result = /JSESSIONID=[A-F0-9]+\.([a-zA-Z0-9]+)/g.exec(document.cookie);
  if (result && result[1]) {
    window._gaq.push(['_setCustomVar', 3, 'alf_server', result[1], 1]);
  }

  window._gaq.push(['_setCustomVar', 4, 'fileApi', (Modernizr.fileinput && Modernizr.filereader) ? "yes" : "no", 1]);
  // end of CNR custom variables

  (function () {
    var ga = document.createElement('script'), s;
    ga.type = 'text/javascript';
    ga.async = true;
    ga.src = ('https:' === document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    s = document.getElementsByTagName('script')[0];
    s.parentNode.insertBefore(ga, s);
  }());

});