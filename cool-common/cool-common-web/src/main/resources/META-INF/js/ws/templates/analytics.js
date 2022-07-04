require(['json!common', 'modernizr'], function (common, Modernizr) {
  "use strict";

  if (common.analytics_url) {
    var _paq = window._paq = window._paq || [];
    /* tracker methods like "setCustomDimension" should be called before "trackPageView" */
    _paq.push(['trackPageView']);
    _paq.push(['enableLinkTracking']);
   (function() {
      var u = common.analytics_url;
      _paq.push(['setTrackerUrl', u+'matomo.php']);
      _paq.push(['setSiteId', common.analytics_siteId]);
      var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];
      g.async=true; g.src=u+'matomo.js'; s.parentNode.insertBefore(g,s);
    })();
  }

});
