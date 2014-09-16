define(['jquery', 'json!common', "cnr/cnr", "cnr/cnr.ui.group", "cnr/cnr.bulkinfo", 'cnr/cnr.ui', 'cnr/cnr.search', 'i18n', 'cnr/cnr.actionbutton', 'cnr/cnr.url', 'cnr/cnr.criteria'], function ($, common, CNR, Group, BulkInfo, UI, Search, i18n, ActionButton, URL, Criteria) {
// "Tasks to do" and "Assigned Tasks"
  "use strict";

  var showWorkflowDetails,
    retrieveAuthority,
    settings,
    propertyIds = {
      notifyMe: '{http://www.cnr.it/model/workflow/1.0}notifyMe',
      groupName: '{http://www.cnr.it/model/workflow/1.0}groupName',
      workflowCNRId: '{http://www.cnr.it/model/workflow/1.0}wfCounterId'
    },
    bulkInfos = {
      task: 'D_bpm_workflowTask',
      workflow: 'D_bpm_workflow',
      workflowTasks: 'D_bpm_workflowTasks'
    },
    defaults = {
      elements: {}
    };

  // general utility functions
  function flatten(item, m, prefix) {
    prefix = prefix || '';
    m = m || {};
    $.each(item || {}, function (key, value) {
      var suffix = prefix ? (prefix + '_' + key) : key;
      if (typeof value === 'object' && !Array.isArray(value)) {
        flatten(value, m, suffix);
      } else {
        m[suffix] = value;
      }
    });
    return m;
  }

  function flatAndMerge() {
    var flattened = $.map(arguments, function (el) {
      return flatten(el);
    });
    return $.extend.apply(null, flattened);
  }

  // display user's tasks and workflows started by the current user filtered by @param {string} office
  function displayTaskLists(office) {
    // my tasks to do
    var xhrMyTasks = URL.Data.proxy.tasks({
      data: {
        authority: common.User.id,
        maxItems: 0
      }
    }),
      xhrAssignedTasks = URL.Data.proxy.workflowInstances({
        data: {
          initiator: common.User.id,
          state: 'active'
        }
      });

    $.when(xhrMyTasks, xhrAssignedTasks).done(function (xhrTaskData, xhrWorkflowData) {

      var taskData = xhrTaskData[0].data,
        workflowData = xhrWorkflowData[0].data;

      URL.Data.proxy.workflowProperties({
        traditional: true,
        data: {
          assignedByMeWorkflowIds: $.map(workflowData, function (el) {
            return el.id;
          }),
          properties: $.map(propertyIds, function (value) {return value; })
        }
      }).done(function (xhrPropertiesData) {

        // display task or workflow list
        function displayTaskList(div, bulkInfoId, taskData, customProperties) {

          var label = div.find("span.empty"), rows;

          div.find('.bulkInfo').remove();

          rows = $.map(taskData, function (row) {
            var metadata = customProperties[row.id] || {};
            if (!office || metadata[propertyIds.groupName] === office) {
              row.metadata = {};

              $.each(metadata, function (index, el) {
                row.metadata[index.replace(/^\{.*\}/g, 'wfcnr_')] = el;
              });

              return flatAndMerge({}, row, metadata);
            }
          });

          new BulkInfo({
            target: div,
            handlebarsId: 'task',
            path: bulkInfoId,
            metadata: rows,
            handlebarsSettings: {button: {label: 'informazioni'}}
          }).handlebars();

          if (rows.length) {
            label.hide();
          } else {
            label.show();
          }
        }

        // my tasks to do
        displayTaskList(settings.elements.myTasks, bulkInfos.task, taskData, xhrPropertiesData.mine);

        // workflow started by the current user
        displayTaskList(settings.elements.assigned, bulkInfos.workflow, workflowData, xhrPropertiesData.theirs);
      });

    });



  }

  function init(options) {
    settings = $.extend({}, defaults, options);

    settings.elements.myTasks
      .on('click', 'button.taskButton', function () {
        var props = $(this).data('properties'),
          workflowInstanceId = props.workflowInstance_id,
          task = {id: props.id, name: props.name, title: props.title};

        showWorkflowDetails(workflowInstanceId, props.properties_bpm_package, props, task);
      })
      .on('click', 'button.takeTaskButton', function () {
        var data  = $(this).data();
        URL.Data.proxy.task({
          type: "PUT",
          placeholder: {
            taskId: data.id
          },
          contentType: 'application/json',
          data: JSON.stringify({
            "cm_owner": data.owner || null
          })
        }).done(function () {
          displayTaskLists();
        });

      });

    settings.elements.assigned
      .on('click', '.btn', function () {
        var props = $(this).data('properties'),
          workflowInstanceId = props.id;

        showWorkflowDetails(workflowInstanceId, props['package'], props);
      });
  }
  // create filter by office and display tasks and workflows lists
  function display() {
    new Group.Widget('groups', 'Filtro per ufficio', function (el) {
      displayTaskLists(el.fullName);
    }).appendTo(settings.elements.taskFilters);
  }

  // display documents in package @param {string} packageId
  function displayPackage(packageId, elements) {

    // actions available on package's documents.
    var s = new Search({
      elements: elements,
      display: {
        row : function (el) {
          el.id = el.id ? el.id.split(';')[0] : el.id;

          var li = $('<li class="documentRow"> <a href="' + URL.urls.search.content + '?nodeRef=' + el.id + '">' + el.name + '</a></li>');

          ActionButton.actionButton({
            name: el.name,
            nodeRef: el.id,
            baseTypeId: el.baseTypeId,
            objectTypeId: el.objectTypeId,
            mimeType: el.contentType,
            allowableActions: el.allowableActions
          }, null, {
            workflow: false,
            remove: false
          }).prependTo(li);

          return li;
        }
      }
    }), criteria = new Criteria();

    criteria.inFolder(packageId).list(s);
  }

  // actions panel, show workflow @param {integer} workflowInstanceId details in modal window
  showWorkflowDetails = function (workflowInstanceId, packageId, props, task) {

    var modal = $('<div></div>'),
      ul = $('<ul class="unstyled"></ul>'),
      actionContainer = $('<div></div>'),
      actions = $('<h5>Azioni</h5>'),
      spinnerActions = $('<i class="icon-spinner icon-spin" id="spinner-actions"></i>'),
      labelElement = $('<label class="empty">nessun elemento</label>'),
      properties = $('<div></div>'),
      diagramURL,
      title;

    // show definition diagram if workflow is completed, workflow instance diagram otherwise.
    if (props.endDate) {
      diagramURL = URL.template(URL.urls.proxy.definitionDiagram, {
        workflowDefinitionId: props.definitionUrl.split('/').pop()
      });
    } else {
      diagramURL = URL.template(URL.urls.proxy.diagram, {
        workflow_instance_id: workflowInstanceId
      });
    }

    modal
      .addClass('modal-inner-fix')
      .append(properties)
      .append(actionContainer)
      .append('<h5>Elementi</h5>')
      .append(labelElement)
      .append(ul)
      .append('<img src="' + diagramURL + '"/>'); // workflow diagram

    title = props[propertyIds.workflowCNRId.replace(/[^a-z0-9]/gi, '_')]
      || props.properties_wfcnr_wfCounterId
      || workflowInstanceId;

    UI.modal(title, modal);

    if (task) {

      new BulkInfo({
        target: properties,
        handlebarsId: 'task',
        path: bulkInfos.task,
        metadata: props
      }).handlebars();

      // actions / task transitions
      actionContainer
        .append(actions)
        .append(spinnerActions);

      //load task details
      URL.Data.proxy.task({
        placeholder: {
          taskId: task.id
        },
        data: {
          detailed: true
        }
      }).pipe(function (data) {
        //load transition choices
        return URL.Data.bulkInfo({
          placeholder: {
            path: 'D:' + data.data.definition.id,
            kind: 'form',
            name: 'default'
          },
          data: {
            guest : true
          }
        });
      }).done(function (transitionsData) {
        spinnerActions.remove();

        var outcomeKey =  'wfcnr:reviewOutcome',
          transitions = transitionsData['default'][outcomeKey],
          taskDone = [{
            key: 'Done',
            label: 'Eseguito'
          }]; // "task done" will be the default transition, if no transition has been defined

        function endTask(bulkinfo, transitionKey) {

          // execute transition
          var taskInput = bulkinfo.getData();

          if (!bulkinfo.validate()) {
            UI.error("Campi non validi");
            return false;
          }

          retrieveAuthority(taskInput).done(function (content) {

            content.prop_transitions = "Next";
            content["prop_" + outcomeKey.replace(':', '_')] = transitionKey;

            $.each(taskInput, function (index, item) {
              content["prop_" + item.name.replace(':', '_')] = item.value;
            });

            URL.Data.proxy.endTask({
              placeholder: {
                taskId: task.id
              },
              type: 'POST',
              contentType: 'application/json',
              data: JSON.stringify(content),
              success: function () {
                displayTaskLists();
              }
            });
          });
        }

        $.each(transitions ? transitions.jsonlist : taskDone, function (_, transition) {
          // create and append transition button
          var primary = transitions ? (transition.key === transitions['default']) : true;
          $('<a class="btn btn-mini" data-dismiss="modal"></a> ')
            .tooltip({
              title: transition.tooltip,
              container: actionContainer
            })
            .append('<i class="icon-cog' + (primary ? " icon-white" : "") + '"></i> ')
            .append(i18n.prop(task.name + '_' + task.title.replace(/[^a-z]/gi, '_') + '.transition.' + (transition.key || 'end') + '.title', transition.label))
            .insertAfter(actions)
            .after(' ')
            .addClass(primary ? "btn-primary" : "")
            .on('click', function () {

              var target = $('<div class="modal-inner-fix"></div>'), bulkInfo;

              bulkInfo = new BulkInfo({

                target: target,
                path: transitionsData.cmisObjectTypeId,
                kind: 'form',
                name: transition.key
              });

              UI.modal(i18n.prop(transition.key), target, function () {
                endTask(bulkInfo, transition.key);
              });

              bulkInfo.render();

            });
        });
      });
    } else {

      URL.Data.proxy.workflowInstance({
        placeholder: {
          workflow_instance_id: workflowInstanceId
        },
        data: {
          includeTasks: true
        }
      }).done(function (data) {

        function sorting(a, b) {
          return a.properties.cm_created > b.properties.cm_created;
        }

        data = data.data.tasks.sort(sorting);

        data = $.map(data, function (el) {
          return flatten(el);
        });

        properties.addClass('zebra');

        new BulkInfo({
          target: properties,
          handlebarsId: 'taskHistory',
          path: bulkInfos.workflowTasks,
          metadata: data
        }).handlebars();
      });
    }

    displayPackage(packageId, {
      table: ul,
      label: labelElement
    });
  };

  function executeStartWorkflow(settings, processName) {

    return URL.Data.proxy.startWorkflow({
      placeholder: {
        workflowName: encodeURIComponent(processName)
      },
      data: JSON.stringify(settings),
      processData: false,
      contentType: 'application/json',
      type: 'POST'
    }).done(function (data) {
      if (data.persistedObject) {

        var re = /id=([a-z0-9\$]+)/gi, id = re.exec(data.persistedObject)[1],
          cnrId,
          qname = "{http://www.cnr.it/model/workflow/1.0}wfCounterId";

        URL.Data.proxy.workflowProperties({
          traditional: true,
          data: {
            properties: [qname],
            ids: [id]
          }
        }).then(function (props) {
          cnrId = props.theirs[id][qname];
          UI.info('workflow ' + cnrId + ' avviato con successo');
        }, function () {
          UI.info('workflow ' + processName + ' avviato con successo');
        });
      } else {
        UI.error('impossibile avviare il workflow');
      }
    });
  }

  retrieveAuthority = function (formData) {
    var xhr, groupAssignee, assignee;

    groupAssignee = $.grep(formData, function (el) {
      return el.id === 'bpm:groupAssignee';
    });

    assignee = $.grep(formData, function (el) {
      return el.id === 'bpm:assignee';
    });

    if (groupAssignee.length) {
      xhr = URL.Data.proxy.groups({
        data: {
          filter: $('#bpm\\:groupAssignee').val()
        }
      });
    } else if (assignee.length) {
      xhr = URL.Data.proxy.person({
        data: {
          filter: $('#bpm\\:assignee').val()
        }
      });
    } else {
      xhr = $.Deferred();
      xhr.resolve();
    }

    return xhr.pipe(function (data) {

      var settings = {};

      if (groupAssignee.length) {
        settings.assoc_bpm_groupAssignee_added = data.groups[0].nodeRef;
      } else if (assignee.length) {
        settings.assoc_bpm_assignee_added = data.people[0].nodeRef;
      }

      return settings;
    });

  };

  function startWorkflow(btn, nodes, formData, processName) {

    retrieveAuthority(formData).done(function (settings) {

      settings.assoc_packageItems_added = nodes;

      $.map(formData, function (value, key) {
        if (value.name) {
          if (value.name !== 'bpm:groupAssignee') {
            settings["prop_" + value.name.replace(":", "_")] = (typeof value.value === "boolean") ? (value.value.toString()) : value.value;
          }
        }
      });

      executeStartWorkflow(settings, processName).done(function (data) {
        if (data.persistedObject) {
          btn.attr("disabled", "disabled");
        }
      });
    });

    return false;
  }

  return {
    init: init,
    display: display,
    showWorkflowDetails: showWorkflowDetails,
    startWorkflow: startWorkflow
  };
});
