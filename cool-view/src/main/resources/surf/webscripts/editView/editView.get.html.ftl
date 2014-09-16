<div id="editbulkinfo-main" class="container-fluid">
  <div class="list-main-call">
    <div class="tree">
      <div class="spinner-container">
        <i class="icon-spinner icon-spin icon-2x" id="spinner-tree"></i>
      </div>
    </div>
    <br />

    <br />
    <div class="bulkinfoEdit">
      <select id="select-kind">
        <option id="find">find</option>
        <option id="form">form</option>
        <option id="column">column</option>
      </select>
      <select id="select-find">
      </select>
      <select id="select-form" style="display:none;">
      </select>
      <select id="select-column" style="display:none;">
      </select>
      <div id="bulkinfoEditArea" class="edittextarea" >
        <bulkInfo xmlns="http://www.cnr.it/schema/BulkInfo"></bulkInfo>
      </div>
      <div class="text-center">
        <button id="ricarica-button" class="btn btn-primary"><i class="icon-refresh"></i> ${message('ricarica.button')}</button>
      </div>
    </div>
    <div class="bulkinfo"></div>
      <br />
  </div>



  <!--
  <div class="span8 list-main-call">
    <table class="table" id="items">
          <thead><tr>
            <th>
              <h3>Risultati ricerca</h3>
            </th>
            <th>
              <div id="orderBy" class="btn-group">
                <a class="btn btn-mini dropdown-toggle" data-toggle="dropdown" href="#">
                  ${message('button.order.by')}
                  <span class="caret"></span>
                </a>
                <ul class="dropdown-menu"></ul>
              </div>
            </th></tr>
          </thead>
      </table>
        <div id="itemsPagination" class="pagination pagination-centered">
          <ul></ul>
        </div>
        <div id="emptyResultset" class="alert" style="display:none">${message('message.no.call')}</div>
  </div>
  -->
</div>