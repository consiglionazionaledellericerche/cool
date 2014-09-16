define(['cnr/cnr.ui.priority', 'cnr/cnr.ui.duedate', 'cnr/cnr.ui.group', 'cnr/cnr.ui.radio', 'cnr/cnr.ui.select', 'cnr/cnr.ui.checkbox', 'cnr/cnr.ui.datepicker', 'cnr/cnr.ui.datetimepicker', 'cnr/cnr.ui.country', 'cnr/cnr.ui.city', 'cnr/cnr.ui.authority', 'cnr/cnr.ui.workflow', 'cnr/cnr.ui.tree', 'cnr/cnr.ui.sedi'], function (priority, duedate, group, radio, select, checkbox, datepicker, datetimepicker, country, city, authority, workflow, tree, sedi) {

  "use strict";

  return {
    'ui.priority': priority,
    'ui.duedate': duedate,
    'ui.group': group,
    'ui.radio': radio,
    //'ui.wysiwyg': wysiwyg,
    // il widget wysiwyg (e le relative dipendenze di ckeditor)
    // devono essere caricate solo in alcune pagine specifiche (e.g. createModify.js)
    'ui.select': select,
    'ui.checkbox': checkbox,
    'ui.datepicker': datepicker,
    'ui.datetimepicker': datetimepicker,
    'ui.country': country,
    'ui.city': city,
    'ui.authority': authority,
    'ui.sedi': sedi,
    'ui.tree': tree,
    'ui.workflow': workflow
  };
});