#!/bin/bash

ABSOLUTE_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
profile=$1
container_name="55g-auth-live"
image_name="55g-auth-server"
container_postfix="live"
spring_env="live,docker"
network_bridge="55g-live"
server_port=8300

if [ "$profile" == "--dev" ]; then
  container_name="55g-auth-dev"
  spring_env="dev,docker"
  container_postfix="dev"
  network_bridge="55g-dev"
  server_port=8350
fi

cd $ABSOLUTE_PATH

docker_ps=$(docker ps --all --filter "name=${container_name}" | awk '{ print $1 }')

docker_network_live_ps=$(docker network ls | grep '55g-live')
if [ -z "$docker_network_live_ps" ]; then
  docker network create 55g-live
fi

docker_network_dev_ps=$(docker network ls | grep '55g-dev')
if [ -z "$docker_network_dev_ps" ]; then
  docker network create 55g-dev
fi

i=0
for line in $docker_ps; do
  ps_arr[i]=$line
  i=$((i + 1))
done

for ((i = 1; i < ${#ps_arr[@]}; i++)); do
  echo "Removing container ${ps_arr[i]}..."
  docker stop ${ps_arr[i]}
  docker rm ${ps_arr[i]}
done

echo "Building docker image..."
docker build -t $image_name-$container_postfix .

echo "Creating container for service..."
docker run -d --name $container_name \
  --network $network_bridge \
  --env SPRING_PROFILE=$spring_env \
  --env SERVER_PORT=$server_port \
  -p $server_port:$server_port \
  -v /logs:/logs \
  -v /var/55g/static:/static \
  $image_name-$container_postfix

echo "Pruning images..."
docker image prune --force

