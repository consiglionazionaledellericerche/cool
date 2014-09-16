<div id="rbac-admin-main">
	<div id="rbac-admin-data" style="width: 90%; margin: 40px auto;">
		<div class="form-horizontal">
			<div class="control-group">
				<button class="btn btn-primary" id="rbac-admin-button-add"><i class="icon-edit"></i> ${message('aggiungi-nuovo-permesso')}</button>
			</div>
			<div class="input-append control-group">
				<input type="text" id="rbac-admin-filter" class="search" placeholder="${message('rbac-admin-filtra')}" />
				<button class="btn icon-filter disabled" ></button>
			</div>
		</div>
		<br /><br />
		<table class="table table-striped table-hover table-bordered table-condensed" id="rbac-admin-table-data" style="opacity: 0;">
			<thead id="rbac-admin-table-data-head">
				<th>id</th>
				<th>method</th>
				<th>list</th>
				<th>type</th>
				<th>authority</th>
				<th></th>
			</thead>
			<tbody id="rbac-admin-table-data-body" class="list">
			</tbody>
		</table>
	</div>
</div>