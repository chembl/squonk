#!/bin/sh
#
# sets up the containers ready for use.
# once comple run them with run-containers.sh

if [ ! $PUBLIC_HOST ]; then
	echo "environment variables not set? Run 'source setenv.sh' to set them"
	exit 1
fi

base=$PWD

echo "Setting up for server private:${PRIVATE_HOST} public:${PUBLIC_HOST}"

# substitute the realm json file need by keycloak
sed "s/__public_host__/${PUBLIC_HOST}/g" images/squonk-realm.json.template > images/squonk-realm.json
# set up the proxy details in the tomcat apps 
sed "s/__public_host__/${PUBLIC_HOST}/g" images/xwiki/server.xml.template > images/xwiki/server.xml
# setup xwiki connection to postgres
sed "s/__postgres_xwiki_password__/${POSTGRES_XWIKI_PASS}/g" images/xwiki/hibernate.cfg.xml.template > images/xwiki/hibernate.cfg.xml

images="rabbitmq postgres keycloak xwiki"

docker-compose stop
docker-compose rm -f $images
docker-compose build $images


echo "preaparing postgres docker image ..."
docker-compose -f docker-compose.yml -f docker-compose-setup.yml up -d postgres rabbitmq

# we need to wait for postgres to start as the next step is to populate the database
attempt=0
until nc -z -w 1 $PRIVATE_HOST 5432
do
    if [ $attempt -gt 10 ]; then 
        echo "Giving up on postgres"
	    docker-compose stop
	exit 1
    fi
    echo "waiting for postgres container..."
    sleep 1
    attempt=$(( $attempt + 1 ))
done
echo "postgres is up"

# now we can start keycloak (needs postgres to be setup before it starts)
docker-compose -f docker-compose.yml -f docker-compose-setup.yml up -d keycloak


echo "preparing rabbitmq docker image ..."
./images/rabbitmq/rabbitmq-setup.sh deploy_rabbitmq_1
echo "... rabbitmq container configured"
docker-compose stop rabbitmq

keycloak_url="http://${PRIVATE_HOST}:8080/auth"
echo "keycloak_url: $keycloak_url"



attempt=0
until $(curl --output /dev/null -s -k --head --fail ${keycloak_url}); do
	if [ $attempt -gt 30 ]; then 
        echo "Giving up on keycloak"
		exit 1
		fi
	attempt=$(( $attempt + 1 ))
  	echo 'waiting for keycloak container ...'
  	sleep 1
done
echo "keycloak is up"

token=$(curl -s -k -X POST "${keycloak_url}/realms/master/protocol/openid-connect/token" -H "Content-Type: application/x-www-form-urlencoded" -d "username=admin" -d "password=${KEYCLOAK_PASSWORD:-squonk}" -d "grant_type=password" -d "client_id=admin-cli" | jq -r '.access_token')
echo "token: $token"

curl -s -k -X POST -T images/squonk-realm.json "${keycloak_url}/admin/realms" -H "Authorization: Bearer $token" -H "Content-Type: application/json"
echo "squonk realm added to keycloak"

docker-compose stop
echo finished