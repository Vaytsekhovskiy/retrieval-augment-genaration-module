#!/bin/bash
set -euo pipefail

server_connection="adminn@192.168.9.121"
home_path="/home/adminn/"

# Дамп базы
ssh "$server_connection" "docker exec -i electronhelper_service-db_1 pg_dump -U postgres postgres > $home_path/dump.dump"
rsync -avz --progress -e "ssh -i ~/.ssh/id_rsa" "$server_connection:$home_path/dump.dump" "./"
