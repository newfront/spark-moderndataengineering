# Chapter 15: Part 1: Introduction to Kubernetes
In order to walk through the chapter exercises and take part in the lessons you will have to install and start `Minikube`.

## Installation Guide (MiniKube):
> The smallest usable `Kubernetes` cluster that can handle this chapter material is a  `4 core` x `8g ram` configuration.

## Volumes & Docker & Minikube Synergy
You can forward mount local volumes using the `--mount --mount-string {host-path:vm-path}`. You can use this technique to reuse the data you've created in earlier chapters that are located in your `~/dataengineering` directory. I suggest creating a new directory `mkdir -p ~/dataengineering/k8s` that can be used to host data resources, like a directory for `redis`, `mysql`, `minio`, etc.

> Tip: You can copy the contents of your `MySQL` database to skip additional bootstrapping. `mkdir -p ~/dataengineering/k8s/mysql` and copy the `mysqldir` into the new directory. You can do the same for the `data` directory for `MinIO` too.

## Starting your MiniKube Cluster with Local Volume Mounting
```
minikube start \
  --kubernetes-version v1.21.2 \
  --mount \
  --mount-string ~/dataengineering/k8s/:/mnt/local/data/ \
  --network-plugin=cni \
  --cni calico \
  --embed-certs \
  --memory 32g \
  --cpus 5 \
  --disk-size 80g
```
> Note: Take all CPU Cores on your host machine (minus 1 for OS). Mine is 5 for a 6 core machine. Take 1/2 of your total system RAM (or leave at least 1 for the host OS).

> Tip: Minikube will mount the host volumes with the user (uid) / group (gid) of `docker:docker` which has the nobody user/group (`1000:1000`). In order to access the volumes, you need to use a `securityContext` to `runAsUser: 1000` or `runAsGroup: 1000`.

## Installation Steps
Open up a terminal window (example: Visual Studio Code -> Terminal -> New Terminal) and `cd intro-to-kubernetes`.

```
kubectl apply -f namespace.yaml
```

### Create the Persistent Volume (PV) for Redis
Create a non-ephemeral volume for redis and scratch space storage on your host system.
```
mkdir -p ~/dataengineering/k8s/redis
```

> Note: The example PV and PVC use the `/mnt/local/data/redis` hostPath. The location that is mapped to your systems `~/dataengineering/k8s/redis` directory.
 
```
kubectl apply -f volumes/persistent-volume.yaml
kubectl apply -f volumes/persistent-volume-claim.yaml
```

```
kubectl get pods -l app=redis-pv --all-namespaces
```
