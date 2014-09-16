# Creazione utenze Jconon #

	USER=admin
	PASS=admin
	HOST=http://win:8080/alfresco
	
	curl -u$USER:$PASS $HOST/service/api/people \
	  -d '{"userName":"spaclient", "password":"sp@si@n0", "firstName":"spaclient", "lastName":"spaclient","email":"spaclient"}' \
	  -H "Content-type: application/json"
	
	curl -u$USER:$PASS $HOST/service/api/people \
	  -d'{"userName":"jconon", "password":"jcononpw", "firstName":"jconon", "lastName":"jconon","email":"jconon"}' \
	  -H "Content-type: application/json"
	
	curl -u$USER:$PASS $HOST/service/api/groups/ALFRESCO_ADMINISTRATORS/children/spaclient \
	  -X POST \
	  -H "Content-type: application/json"
