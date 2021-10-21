# Chapter 14: Deploying Mission Critical Spark Applications on Kubernetes
This is the final chapter of the book, where all your other efforts have led to. This is `Day One`.

# Local Kubernetes via MiniKube
Installation instructions are available at https://minikube.sigs.k8s.io/docs/start/, or you can use `brew install minikube` if you are running on MacOS.

Once `minikube` is installed the fun can begin.

## Start up Simple MiniKube Cluster
Minikube will start up with `2 cpus` and `2gb of memory` by default.
~~~
minikube start
~~~

## Pause Minikube
When you want to stop working or free up more system resources, `pause` the cluster (freeze time).
~~~
minikube pause
~~~

## Unpause Minikube
When you are ready to get back to work, you can run `unpause` to return the cluster back its original state (like you never paused at all)
~~~
minikube unpause
~~~

## Stop Minikube
Stop and clean up.
~~~
minikube stop
~~~

# Creating and Configuring a Namespace for Running Spark on Kubernetes
1. `Namespaces` >  

# Advanced Setup Directions

# Start MiniKube with updated VM Options for Prometheus
~~~
minikube delete && minikube start \
  --vm-driver hyperkit \
  --kubernetes-version v1.21.2 \
  --memory 32g \
  --cpus 4 \
  --bootstrapper kubeadm \
  --extra-config kubelet.authentication-token-webhook=true \
  --extra-config kubelet.authorization-mode=Webhook \
  --extra-config scheduler.address=0.0.0.0 \
  --extra-config controller-manager.address=0.0.0.0 && minikube addons disable metrics-server
~~~

*Example Output*
~~~
😄  minikube v1.22.0 on Darwin 11.6
✨  Using the hyperkit driver based on user configuration
👍  Starting control plane node minikube in cluster minikube
🔥  Creating hyperkit VM (CPUs=4, Memory=32768MB, Disk=20000MB) 
🐳  Preparing Kubernetes v1.21.2 on Docker 20.10.6
    ▪ kubelet.authentication-token-webhook=true
    ▪ kubelet.authorization-mode=Webhook
    ▪ scheduler.address=0.0.0.0
    ▪ controller-manager.address=0.0.0.0
    ▪ Generating certificates and keys ...
    ▪ Booting up control plane ...
    ▪ Configuring RBAC rules ...
🔎  Verifying Kubernetes components...
    ▪ Using image gcr.io/k8s-minikube/storage-provisioner:v5
🌟  Enabled addons: storage-provisioner, default-storageclass
🏄  Done! kubectl is now configured to use "minikube" cluster and "default" namespace by default
🌑  "The 'metrics-server' addon is disabled
~~~

## Minikube kubectl Alias
In order to not pollute any other installations of `kubectl` on your host, you can create a simple alias to the `minikube` controlled `kubectl`. This way you can test out changes to new versions of Kubernetes in an isolated environment.

~~~
alias mk8s="minikube kubectl --"
~~~

### Check the Kubernetes Cluster Config
`% kubectl get node minikube` or `% mk8s get node minikube`

~~~
NAME       STATUS   ROLES                  AGE   VERSION
minikube   Ready    control-plane,master   63s   v1.21.2
~~~

`% kubectl get node minikube -o jsonpath='{.status.capacity}`
~~~
{"cpu":"4","ephemeral-storage":"17784752Ki","hugepages-2Mi":"0","memory":"32934504Ki","pods":"110"}
~~~

### Check the IP of the Minikube Cluster
`% minikube ip`

# Kubernetes Native Package Manager

# Installing Krew
[`krew`](https://krew.sigs.k8s.io/docs/user-guide/setup/install/) is like `brew` for `K8s`.

~~~
(
  set -x; cd "$(mktemp -d)" &&
  OS="$(uname | tr '[:upper:]' '[:lower:]')" &&
  ARCH="$(uname -m | sed -e 's/x86_64/amd64/' -e 's/\(arm\)\(64\)\?.*/\1\2/' -e 's/aarch64$/arm64/')" &&
  KREW="krew-${OS}_${ARCH}" &&
  curl -fsSLO "https://github.com/kubernetes-sigs/krew/releases/latest/download/${KREW}.tar.gz" &&
  tar zxvf "${KREW}.tar.gz" &&
  ./"${KREW}" install krew
)
~~~

## Check the Install
> kubectl krew
~~~
krew is the kubectl plugin manager.
You can invoke krew through kubectl: "kubectl krew [command]..."

Usage:
  kubectl krew [command]

Available Commands:
  completion  generate the autocompletion script for the specified shell
  help        Help about any command
  index       Manage custom plugin indexes
  info        Show information about an available plugin
  install     Install kubectl plugins
  list        List installed kubectl plugins
  search      Discover kubectl plugins
  uninstall   Uninstall plugins
  update      Update the local copy of the plugin index
  upgrade     Upgrade installed plugins to newer versions
  version     Show krew version and diagnostics

Flags:
  -h, --help      help for krew
  -v, --v Level   number for the log level verbosity

Use "kubectl krew [command] --help" for more information about a command.
~~~

## Install Minio the Krew way
Steps taken from the documentation: https://github.com/minio/operator/blob/master/README.md

```
kubectl krew install minio
```
> Note: Like the warning says, if you are going to install things 

```
% kubectl minio version
v4.2.14
```

```
% kubectl minio init
```

```
namespace/minio-operator created
serviceaccount/minio-operator created
clusterrole.rbac.authorization.k8s.io/minio-operator-role created
clusterrolebinding.rbac.authorization.k8s.io/minio-operator-binding created
customresourcedefinition.apiextensions.k8s.io/tenants.minio.min.io created
service/operator created
deployment.apps/minio-operator created
serviceaccount/console-sa created
clusterrole.rbac.authorization.k8s.io/console-sa-role created
clusterrolebinding.rbac.authorization.k8s.io/console-sa-binding created
configmap/console-env created
service/console created
deployment.apps/console created
```

```
kubectl minio proxy -n minio-operator
```


### Credits, Thanks, and Additional Reading
* https://kubernetes.io/docs/tasks/access-application-cluster/ingress-minikube/
* https://www.redhat.com/sysadmin/installing-prometheus
* https://github.com/prometheus-operator/kube-prometheus#minikube
* https://kubernetes.github.io/ingress-nginx/user-guide/monitoring/
* https://docs.min.io/docs/deploy-minio-on-kubernetes.html
* https://github.com/minio/operator/blob/master/README.md
* https://krew.sigs.k8s.io/docs/user-guide/setup/install/
* https://www.shellhacks.com/minikube-start-with-more-memory-cpus/
* https://kubernetes.github.io/ingress-nginx/deploy/rbac/
* https://kubernetes.github.io/ingress-nginx/deploy/
