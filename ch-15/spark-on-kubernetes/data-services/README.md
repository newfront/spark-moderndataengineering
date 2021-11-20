# Setting up MinIO and MySQL 8 on Kubernetes

1. Start off by sshing into the minikube vm.
~~~
minikube ssh
~~~

2. Create a new `hostPath` directory for `mysql` and `minio` respectively.
~~~
sudo mkdir -p /mnt/mysql
sudo mkdir -p /mnt/minio
~~~

> Note: As a follow up, you can also use `hostPath` mounted volumes (in minikube) to reuse any of the data you generated earlier in the book for `MinIO` or `MySQL`. I'd recommend going through the manual process once to familiarize yourself with everything. 

3. Use the `deployment-mysql.yaml` and `deployment-minio.yaml` to bootstrap the services.
~~~
kubectl apply -f deployment-mysql.yaml
kubectl apply -f deployment-minio.yaml
~~~

4. Complete the MySQL bootstrap process `again...`
Locate the `mysql-pv-*` pod so you can run the `bootstrap`. You'll then copy the installation files from the `data-services/install` directory onto the Pod to continue. This process will have to be done once if you are using the internal `minikube` host file system.

From the `ch-15/spark-on-kubernetes` directory. Run the following command.

```
kubectl cp data-services/install mysql-pv-9c749764c-rshwp:/tmp/install -n data-services
```

Then from the `exec` process. You'll need to do the following.
~~~
cd /tmp/install
./bootstrap.sh
~~~

You will be prompted for the ROOT password. See the `mysql-config` ConfigMap from the `deployment-mysql.yaml`. The password (default) is the same as the `docker-compose` from earlier in the book.

> Tip/Caution: The ROOT MySQL password is stored in the `mysql-config ConfigMap` in the `data-services` namespace. While this allows some protection you should look at mounting encrypted `secrets` into the Namespace using `KMS` on `EKS` or alternative secure key management solution so that important passwords aren't available to the masses. 

## MinIO
Find the minio-pv `Pod` so you can port forward and check things out.
~~~
kubectl get pods -n data-services
~~~

**Port Forward to see the MinIO UI**
~~~
kubectl port-forward minio-pv-67899f8d9c-bvvm9 -n data-services 9001:9001
~~~
