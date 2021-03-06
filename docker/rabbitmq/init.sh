#!/bin/bash
#
# this script is copied into the top level dir of the docker image so
# that the environment for tests can be setup
# execute in docker like this:
# docker exec <image_name> bash init.sh

rabbitmqctl delete_vhost /squonk

rabbitmqctl delete_user guest
#rabbitmqctl delete_user admin
rabbitmqctl delete_user squonk

rabbitmqctl add_vhost /squonk

#rabbitmqctl add_user admin  ${RABBITMQ_DEFAULT_PASS:-squonk}
#echo created admin user with password ${RABBITMQ_DEFAULT_PASS:-squonk}
rabbitmqctl add_user squonk ${RABBITMQ_SQUONK_PASS:-squonk}
echo created squonk user with password ${RABBITMQ_SQUONK_PASS:-squonk}

#rabbitmqctl set_user_tags admin administrator

rabbitmqctl set_permissions -p /       admin  ".*" ".*" ".*"
rabbitmqctl set_permissions -p /squonk admin  ".*" ".*" ".*"
rabbitmqctl set_permissions -p /squonk squonk ".*" ".*" ".*"
