#!/bin/bash

HOME=$PWD
echo HOME = $HOME

rm *.war

cd ../../components; ./gradlew --daemon core-services-notebook:build
cd $HOME
cp ../../components/core-services-notebook/build/libs/core-services-notebook-0.2-SNAPSHOT.war notebook.war

cd ../../../portal/portal-app; ant -f portal.xml build-portal-app-zip
cd $HOME
cp ../../../portal/portal-app/dist/portal-app.war ROOT.war

echo "building squonk/portal docker image ..."
docker build -t squonk/portal .
echo "... squonk/portal docker image built"
cd $HOME

echo finished

