define(['cnr/cnr.url', 'cnr/cnr.ui.select'], function (URL, Select) {

  "use strict";

  function widget(id, labelText, item) {

    item.ghostName = "descrizioneSede";
    item.maximumSelectionSize = 2;

    var obj = Select.CustomWidget(id, labelText, item);

    URL.Data.sedi().done(function (data) {
      obj.setOptions(data.results);
    });

    return obj.emptyWidget;

  }

  return {
    Widget: widget
  };
});
