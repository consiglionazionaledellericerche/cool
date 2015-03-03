<html>
<body>
<p>Gentile <b>${account.fullName}</b>,
abbiamo ricevuto una richiesta di cambio password per il suo Nome Utente: <b>${account.userName}</b></p>

<p>Se ha effettuato questa richiesta, attivi il collegamento seguente:</p>

<p><a href="${url}/change-password?userid=${account.userName}&pin=${account.pin}">${url}/change-password?userid=${account.userName}&pin=${account.pin}</a></p>

<p>Questo collegamento funzionerà finchè non cambiera' la password.</p>

<p>Se non aveva richiesto il cambio password, un altro
 utente avrà probabilmente inoltrato questa richiesta per errore.</p>
<p>In questo caso ignori questa email e non verrà fatto nessun cambio al suo account.</p>
<hr/>
<p></p>
</body>
</html>