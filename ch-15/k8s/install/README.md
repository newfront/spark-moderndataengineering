
# Enabling Spark on Kubernetes
In order to run Spark on Kubernetes you need to create an environment to run your application.

1. Create the Spark Namespace
   ```
   mk8s apply -f namespace.yaml
   ```

2. Create a Spark Service Account
   ```
   mk8s apply -f serviceaccount.yaml
   ```

3. Create a RoleBinding so Spark can Orchestrate Itself
   ```
   mk8s apply -f rolebinding.yaml
   ```