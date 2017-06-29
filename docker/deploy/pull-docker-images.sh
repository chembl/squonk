#!/bin/sh

docker pull rabbitmq:3-management
docker pull nginx:1.13
docker pull tomcat:8-jre8

docker pull informaticsmatters/rdkit_cartridge:Release_2016_03_1

docker pull informaticsmatters/rdkit:latest
docker pull informaticsmatters/rdkit:Release_2017_03_1
docker pull informaticsmatters/rdkit_java:latest
docker pull informaticsmatters/rdkit_java:Release_2017_03_1
docker pull informaticsmatters/rdkit_java_tomcat:latest
docker pull informaticsmatters/rdkit_java_tomcat:Release_2017_03_1
docker pull informaticsmatters/rdock_nextflow

docker pull abradle/standardiser
docker pull abradle/smog2016
docker pull abradle/pli
docker pull abradle/obabel
