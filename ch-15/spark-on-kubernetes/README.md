# Chapter 15: Part 2: Spark to Kubernetes
To get started, head on over to the `spark-apps` directory. 

## Create the `spark-apps` Namespace
You'll want a namespace to group your Spark applications together.

**Request**
~~~
kubectl apply -f namespace.yaml
~~~

**Response**
~~~
namespace/spark-apps created
~~~

## RBAC: Create a Role and RoleBinding for the `spark-controller`
In order to use the `Spark Kubernetes Resource Manager` you'll need to create a [`Role`](https://kubernetes.io/docs/reference/access-authn-authz/rbac/#role-example) and [`RoleBinding`](https://kubernetes.io/docs/reference/access-authn-authz/rbac/#rolebinding-and-clusterrolebinding). Together these two resources allow your Apache Spark applications to communicate with the Kubernetes APIs and coordinate allocation and deallocation of resources on behalf of your application through a process called Role Based Access Control.

**Command**
~~~
kubectl apply -f spark-controller.yaml
~~~

**Response**
~~~
serviceaccount/spark-controller created
role.rbac.authorization.k8s.io/spark-app-controller created
rolebinding.rbac.authorization.k8s.io/spark-app-controller-binding created
~~~

## Connect Services from the Namespace `data-services` into the Namespace `spark-apps`
The Redis service was created in Part 1, as a recap, the service was defined as `redis-service.yaml`.
> You can apply it using the EOF statement below as well.

~~~
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Service
metadata:
  name: redis-service
  namespace: data-services
spec:
  selector:
    app: redis
    type: memory
  type: ClusterIP
  ports:
    - name: tcp-port
      port: 6379
      targetPort: 6379
      protocol: TCP
EOF
~~~

This will create the following service fully qualified domain name (FQDN): `redis-service.data-services.svc.cluster.local`. Which we will use in the next section.

## Bridge the Namespaces using an ExternalName Service
By design, services in one namespace are not automatically available in all other namespaces. To bridge this gap, you can create a bridge to an external named service. 
~~~
cat <<EOF | kubectl apply -f -
kind: Service
apiVersion: v1
metadata:
  name: redis-service
  namespace: spark-apps
spec:
  type: ExternalName
  externalName: redis-service.data-services.svc.cluster.local
  ports:
  - port: 6379
EOF
~~~

## View all Services
~~~
% kubectl get services --all-namespaces
~~~

**Output**
~~~
NAMESPACE       NAME              TYPE           CLUSTER-IP       EXTERNAL-IP                                     PORT(S)                      AGE
data-services   redis-service     ClusterIP      10.106.24.62     <none>                                          6379/TCP                     7d22h
default         kubernetes        ClusterIP      10.96.0.1        <none>                                          443/TCP                      8d
kube-system     kube-dns          ClusterIP      10.96.0.10       <none>                                          53/UDP,53/TCP,9153/TCP       8d
spark-apps      redis-service     ExternalName   <none>           redis-service.data-services.svc.cluster.local   6379/TCP                     43h
~~~

## Installing MinIO and MySQL
If you haven't yet gone through the exercise of adding the additional two services into the data-services namespace. You can head over to `ch-15/spark-on-kubernetes/data-services/README.md` to install the two components, and to do any additional bootstrapping.

## Deploying the Redis Powered Spark Structured Streaming Application
The step-by-step guide is available at `ch-15/spark-on-kubernetes/spark-redis-streams/README.md`. 

