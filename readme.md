# Readme

# Envirronement de developpemnt

~~~bash
sudo apt install podman podman-docker openjdk-17-jdk maven
git clone https://github.com/mpatron/docker-spark
cd ~/docker-spark/example/spark-java2
mvn package
cd ../..

# In /etc/containers/registries.conf add :
unqualified-search-registries = ['registry.fedoraproject.org', 'registry.access.redhat.com', 'registry.centos.org', 'docker.io']

docker login docker.io -u mpatron
docker image tag localhost/mpatron/docker-spark docker.io/mpatron/docker-spark:latest
docker image push docker.io/mpatron/docker-spark:latest
~~~

# Construction de l'image docker spark.

~~~bash
vagrant@ubuntu:~/docker-spark$ docker build -t mpatron/docker-spark .
[+] Building 155.6s (14/14) FINISHED
 => [internal] load build definition from Dockerfile                                                                                                                                                        0.0s
 => => transferring dockerfile: 1.43kB                                                                                                                                                                      0.0s
 => [internal] load .dockerignore                                                                                                                                                                           0.0s
 => => transferring context: 2B                                                                                                                                                                             0.0s
 => [internal] load metadata for docker.io/library/ubuntu:20.04                                                                                                                                             1.2s
 => [1/9] FROM docker.io/library/ubuntu:20.04@sha256:aba80b77e27148d99c034a987e7da3a287ed455390352663418c0f2ed40417fe                                                                                       0.0s
 => [internal] load build context                                                                                                                                                                           0.0s
 => => transferring context: 182B                                                                                                                                                                           0.0s
 => CACHED [2/9] RUN apt-get update -y &&  apt-get upgrade -y && apt-get dist-upgrade -y && apt-get autoremove -y && apt-get upgrade -y && apt-get autoremove -y;                                           0.0s
 => [3/9] RUN apt-get install software-properties-common -y && apt-get install openjdk-8-jdk -y && apt-get install curl -y                                                                                 86.8s
 => [4/9] RUN curl -OL https://archive.apache.org/dist/spark/spark-2.4.8/spark-2.4.8-bin-hadoop2.7.tgz &&  tar -xzf spark-2.4.8-bin-hadoop2.7.tgz &&  mv spark-2.4.8-bin-hadoop2.7 /opt/spark              61.7s
 => [5/9] COPY conf/master.conf /opt/conf/master.conf                                                                                                                                                       0.0s
 => [6/9] COPY conf/slave.conf /opt/conf/slave.conf                                                                                                                                                         0.0s
 => [7/9] COPY conf/history-server.conf /opt/conf/history-server.conf                                                                                                                                       0.0s
 => [8/9] COPY conf/spark-defaults.conf /opt/spark/conf/spark-defaults.conf                                                                                                                                 0.0s
 => [9/9] RUN  mkdir -p /opt/spark-events                                                                                                                                                                   0.4s
 => exporting to image                                                                                                                                                                                      5.2s
 => => exporting layers                                                                                                                                                                                     5.2s
 => => writing image sha256:f91706ac44e44b32dbf881f6f0eacc4324d8376a76a4616f58b91acef2cecd2c                                                                                                                0.0s
 => => naming to docker.io/mpatron/docker-spark                                                                                                                                                             0.0s
vagrant@ubuntu:~/docker-spark$ docker images
REPOSITORY             TAG       IMAGE ID       CREATED         SIZE
mpatron/docker-spark   latest    f91706ac44e4   2 minutes ago   1.14GB
vagrant@ubuntu:~/docker-spark$ docker tag b9efb5979927 mpatron/docker-spark:2.4.8
vagrant@ubuntu:~/docker-spark$ docker push mpatron/docker-spark:2.4.8
~~~

Lancer docker-compose une fois que l'image est déposé. Elle est à jour est déplosé sur [dockerhub](https://hub.docker.com/repository/docker/mpatron/docker-spark)

~~~bash
sudo docker-compose build
sudo docker-compose up
~~~

Accéder à la console spark-shell et aussi utiliser [Spark history](http://127.0.0.1:18080)

~~~bash
docker exec -it master /bin/bash
spark-shell --master spark://master:7077
~~~

Eteindre l'ensemble

~~~bash
docker-compose down
~~~


curl -XPOST http://localhost:9200/test-index/test-type/1 -H "Content-Type: application/json" -d '{"message": "This is a test document"}'
curl -X POST -d 'json={"foo":"bar"}' http://fluentd:24224/app.log

Docuementation plugings
https://docs.fluentd.org/input/http

Source du docker de fluentd
https://github.com/fluent/fluentd-docker-image/tree/master/v1.13/alpine

chmod +x ./fluentd/entrypoint.sh
docker-compose build

curl -X POST -d 'json={"foo":"bar"}' http://fluentd:9880/app.log


tar -zcvf "/mnt/c/TEMP/docker-spark-$(date '+%d-%m-%Y_%H-%M-%S').tar.gz" docker-spark
cp /mnt/c/Users/mpatron/eclipse-workspace/log4fluentd/target/log4fluentd-0.0.1-SNAPSHOT-jar-with-dependencies.jar ~/docker-spark/log4fluentd.jar
docker exec -it master /bin/bash
spark-shell 
:load SparkListenerDemo.scala


Sources :

- [Exemple de dockerfile](https://github.com/pavanpkulkarni/docker-spark-image/blob/master/Dockerfile)
- [Script de lancement](https://github.com/pavanpkulkarni/create-and-run-spark-job)
- [Exemple de code spark en scala avec des listeners](https://gist.github.com/vinodkc/9574b55270ba6b7c1369187a5db1d0cb)

pip3 list --outdated --format=freeze | grep -v '^\-e' | cut -d = -f 1 | xargs -n1 pip3 install -U
sudo apt autoclean -y && sudo apt update -y && sudo apt upgrade -y && sudo apt autoremove --purge -y

docker build -t mpatron/docker-spark .
docker-compose up
docker exec -it master /bin/bash
spark-submit --master spark://master:7077 --class org.jobjects.Main  /opt/uber-java-spark-examples-1.0-SNAPSHOT.jar 100
