<div id="gestione-utenti-main" class="row">
	<div class="span4 list-main-call">
		<form id="gestione-utenti-search" class="form-horizontal">
			<div class="control-group">
				<label class="control-label" for="firstName">${message('ricercaUtenti.nome')}</label>
				<div class="controls">
					<input name="firstName" type="text" class="input-medium"/>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="lastName">${message('ricercaUtenti.cognome')}</label>
				<div class="controls">
					<input name="lastName" type="text" class="input-medium"/>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="userName">${message('ricercaUtenti.userName')}</label>
				<div class="controls">
					<input name="userName" type="text" class="input-medium"/>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="matricola">${message('ricercaUtenti.matricola')}</label>
				<div class="controls">
					<input name="matricola" type="text" class="input-medium"/>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="codiceFiscale">${message('ricercaUtenti.codiceFiscale')}</label>
				<div class="controls">
					<input name="codiceFiscale" type="text" class="input-medium"/>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="email">${message('ricercaUtenti.email')}</label>
				<div class="controls">
					<input name="email" type="text" class="input-medium"/>
				</div>
			</div>
			<div class="text-center">
				<button id="gestione-utenti-button" type="submit" class="btn btn-primary"><i class="icon-search"></i> ${message('ricercaUtenti.button')}</button>
			</div>
		</form>
	</div>

	<div class="span8 list-main-call">
		<div class="table-container">
		</div>
	</div>

</div>