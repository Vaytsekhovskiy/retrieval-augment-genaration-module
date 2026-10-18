FROM pgvector/pgvector:pg16

COPY ../DockerFileForDb/init-vector.sql /docker-entrypoint-initdb.d/

WORKDIR /tmp
EXPOSE 5432