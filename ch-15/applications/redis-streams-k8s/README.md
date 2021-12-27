# Chapter 15 : Part2: Spark on Kubernetes

## Build the Application and Use the Minikube Docker Environment for Local Containers

### Step 1: Build the Application
~~~
sbt clean assembly
~~~

### Step 2: Create the Container
In order to use your container within your local `minikube` environment, you will have to first update your Docker env.

You can do this by simply applying the environment information using `minikube docker-env`. 
Then simply build the application image to push the resulting container into the `minikube docker context`. 

~~~
eval $(minikube docker-env)
docker build . -t mde/redis-streams-k8s:1.0.0
~~~

Now the container will be available to run via Spark Submit.

### Step 3: Running on Kubernetes
Assuming you have bootstrapped your environment following the `intro-to-kubernetes` and `spark-on-kubernetes` README's, the final step is to submit our application.

See `ch-15/spark-on-kubernetes/spark-redis-streams/README.md` for the complete step-by-step details. 

### Step 4: Manually Run the Application
After launching your Spark Deployment Pod: `kubectl apply -f spark-on-kubernetes/deployment-manual.yaml`, just open up an `exec` session to go ahead and run the launch command.

**Manually Trigger the Spark Submit Command**

From within the `exec` process on the Pod.
~~~
/opt/spark/app/conf/spark-submit.sh
~~~
> The spark-submit.sh executable ships with the Docker container and can be found in `/ch-15/applications/redis-streams-k8s/conf/spark-submit.sh`
