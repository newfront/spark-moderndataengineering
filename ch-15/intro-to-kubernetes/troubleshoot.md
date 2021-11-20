## Redis Isn't Persisting

### 1. Find the Redis Pod Deployment

```
kubectl get pods -l app=redis-pv -n data-services
NAME                                   READY   STATUS    RESTARTS   AGE
redis-deployment-pv-849c64f7f4-dnzq6   1/1     Running   0          30s
```

### 2. Pop into the exec shell
```
kubectl exec -it redis-deployment-pv-849c64f7f4-dnzq6 -n data-services -- bash
```

### 3. Add a Key & Force a Background Save (bgsave)
```
I have no name!@redis-deployment-pv-849c64f7f4-dnzq6:/data$ redis-cli
127.0.0.1:6379> keys *
(empty array)
127.0.0.1:6379> set name scott
OK
127.0.0.1:6379> get name
"scott"
127.0.0.1:6379> bgsave
Background saving started
127.0.0.1:6379> lastsave
(integer) 1637365493
127.0.0.1:6379> exit
```

### 4. Check that the `dump.rdb` exists in the `/data` workdir.
You'll notice that since the `redis` Pod is running with the docker user and group that the save file written to the hostPath volume has the uid/gid of 1000:1000.

```
I have no name!@redis-deployment-pv-849c64f7f4-dnzq6:/data$ ls -l
total 1
-rw-r--r-- 1 1000 1000 109 Nov 19 23:44 dump.rdb
```

Hopefully this solves issues with persistent volumes.