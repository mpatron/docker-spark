
docker build -t mpatron/docker-spark .

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


docker-compose build
docker-compose up
docker exec -it master /bin/bash



Sources : 
- https://github.com/pavanpkulkarni/docker-spark-image/blob/master/Dockerfile
- https://github.com/pavanpkulkarni/create-and-run-spark-job
- https://gist.github.com/vinodkc/9574b55270ba6b7c1369187a5db1d0cb

sudo apt-get install openjdk-8-jdk -y

spark-shell --master spark://master:7077

docker-compose down
