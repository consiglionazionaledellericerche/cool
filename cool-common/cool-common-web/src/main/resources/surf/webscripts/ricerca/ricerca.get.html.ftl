<div id="ricerca-avanzata-main" class="row">
	<div class="span4 offset1">
	 <div class="control-group ">
    <br />	 
		<div class="tree"></div>
		<br />
		<div class="aspect-list"></div>
		<br />
		<form id="search-form" class="form-horizontal">
			<div class="control-group bulkinfo"></div>
			<div class="control-group bulkinfoAspect"></div>
		</form>
    <div class="controls">
      <button id="applyFilter" type="button" class="btn btn-primary"><i class="icon-filter icon-white"></i> Filtra</button>
      <button id="resetFilter" type="button" class="btn"><i class="icon-repeat"></i> Ricarica</button>
    </div>
   </div>
	</div>

	<div class="span7 list-main-call">
		<table class="table" id="items">
      <thead>
          <tr>
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
            </th>
          </tr>
       </thead>
       <tbody></tbody>
	  </table>
    <div id="itemsPagination" class="pagination pagination-centered">
      <ul></ul>
    </div>
    <div id="emptyResultset" class="alert" style="display:none">${message('message.no.call')}</div>
	</div>
	
</div>