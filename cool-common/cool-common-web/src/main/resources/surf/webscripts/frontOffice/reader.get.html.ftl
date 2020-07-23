<div class="header container">
    <div class="jumbotron"><h1>${message('page.frontOffice')}</h1></div>
</div>
<div class="container">
    <div class="row">
        <div class="span4 offset1">
              <div class="control-group ">
                <br>
                <label class="control-label" for="typeDocument">Scegliere il tipo di reader da caricare</label>
                <div class="controls">
                    <div class="btn-group" id="typeDocument" data-toggle="buttons-radio">
                      <button type="button" data-state='notice' class="btn btn-small">Avvisi</button>
                      <button type="button" data-state='log' class="btn btn-small" >Log</button>
                      <button type="button" data-state='faq' class="btn btn-small">FAQ</button>
                    </div>
                </div>
                <br>
                <br>
                    <div class="controls" id="createModify">
                    </div>
                <br>
                <br>
              </div>
                <form id="filters" class="form-horizontal">
                    <h4> Filtri per la selezione</h4>
                    <div id="filtriDiv"></div>
                    <div class="control-group">
                        <div class="btn-group"  id="bottoni">
                            <button id="applyFilter" class="btn btn-small "><i class="icon-filter icon-white"></i> Filtra</button>
                            <button id="resetFilter" class="btn btn-small"><i class="icon-repeat"></i> Reset</button>
                        </div>
                    </div>
                </form>
        </div>
        <div class="span6">
            <div class="zebra" id="docs"></div>
        </div>
    </div>
</div>