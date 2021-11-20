# Chapter 15: Deploying Mission Critical Spark Applications on Kubernetes
This is the final chapter of the book, where all your other efforts have led to. This is `Day One`, and I hope you've been enjoying the journey here.

# Install the Kubernetes Command Line `kubectl`
The Kubernetes command line tool (the kubetcl) will be your friend in your journey to K8s greatness.

https://kubernetes.io/docs/tasks/tools/#kubectl

## View the Commands
View the available helpers.
~~~
kubectl --help
~~~

## Adding Type-Ahead to your Kubectl (`zsh`)
This can help when you are learning the Kuberetes verbs and resources

https://kubernetes.io/docs/tasks/tools/included/optional-kubectl-configs-zsh/



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

# Hands-On Exercises
The hands-on final chapter exercises take you through the process of building up your local `kubernetes` environment. The lessons learned will enable you to clearly conceptualize how cloud-native Apache Spark operates, giving you all the tools (at your fingertips) to deploy, automatically recover from failure, as well as operate your Spark Structured Streaming applications on elastically scalable distributed compute.

## Part 1: Intro to Kubernetes
The first half of the chapter introduces you to the common components of Kubernetes, and walks you through setting up your local environment (piece by piece). 

Materials: `/intro-to-kubernetes/README.md`

## Part 2: Spark on Kubernetes

Materials: `/spark-on-kubernetes/README.md`
