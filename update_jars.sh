#!/bin/bash
set -euo pipefail

project_path="/c/projects/ElectronHelper"
server_connection="adminn@192.168.9.121"
home_path="/home/adminn/"

# Список микросервисов в формате: project_name project_jar_name docker_jar_name container_name
services=(
    "ApiGateway ApiGateway.jar gateway.jar electronhelper_api-gateway_1"
    "BotMicroservice BotMicroservice.jar bot.jar electronhelper_bot-microservice_1"
    "ConvertMicroservice ConvertMicroservice.jar convert.jar electronhelper_convert-microservice_1"
    "DiscoveryServer DiscoveryServer.jar discovery.jar electronhelper_discovery-server_1"
    "MainMicroservice MainMicroservice.jar main.jar electronhelper_main-microservice_1"
    "TelegramBot TelegramBot.jar tg.jar electronhelper_tg-bot_1"
)

# rsync сборка и копирование джарников на сервер
for svc in "${services[@]}"; do
    read -r project_name project_jar docker_jar container <<< "$svc"
    echo "Copying $project_jar to server..."
    rsync -avz --progress -e "ssh -i ~/.ssh/id_rsa" \
        "$project_path/$project_name/target/$project_jar" \
        "$server_connection:$home_path"

    # Переименование на сервере в docker-джар
    ssh "$server_connection" "mv $home_path/$project_jar $home_path/$docker_jar"

    # Резервная копия из контейнера
    ssh "$server_connection" "docker cp $container:/app/$docker_jar old_$docker_jar"
done

# Дамп базы
ssh "$server_connection" "docker exec -i electronhelper_service-db_1 pg_dump -U postgres postgres > $home_path/dump.dump"
rsync -avz --progress -e "ssh -i ~/.ssh/id_rsa" "$server_connection:$home_path/dump.dump" "./"

# Копируем новые джарники в контейнеры
for svc in "${services[@]}"; do
    read -r _ _ docker_jar container <<< "$svc"
    ssh "$server_connection" "docker cp $home_path/$docker_jar $container:/app"
done

# Рестарт docker-compose
ssh "$server_connection" "cd $home_path/ElectronHelper && docker-compose restart"