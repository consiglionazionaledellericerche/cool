<div class="container">
  <div class="container-fluid">
    <div class="row-fluid">
      <div class="span4">
        <div id="collection-tree"></div>
      </div><!--/span-->
      <div class="span8">
        <h5>Utenti</h5>
        <ul class="breadcrumb breadcrumb-white explorerItem"></ul>
        <button id="createGroup" class="btn btn-mini explorerItem"><i class="icon-plus"></i> Nuovo gruppo</button>
        <button id="createAssociation" class="btn btn-mini explorerItem"><i class="icon-resize-small"></i> Crea associazione</button>

        <table class="table table-striped" id="items">
          <thead><tr><th>${message('label.document')}</th><th>${message('label.actions')}</th></tr></thead>
        </table>
        <div id="itemsPagination" class="pagination pagination-centered">
          <ul></ul>
        </div>
        <br/><br/>
        <p><span id="emptyResultset" class="label label-info">nessun elemento</span>
        
      </div><!--/span-->
    </div><!--/row-->
  </div>
</div> <!-- /container -->
