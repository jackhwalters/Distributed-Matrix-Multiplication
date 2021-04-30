## Disributed Matrix Multiplication
This is a RESTful distributed matrix multiplication system utilising gRPC to implement the divide and conquer matrix multiplication algorithm.

### Running the system

1. Adjust the IP addresses of the servers in client/src/main/java/com/example/grpc/client/grpcclient/GRPCClientService.java  

2. Generate a nxn matrix (where n is a power of two) with
```
$ python generateMatrix.py n
```

3. Run the client with  
```
$ client/mvnw clean spring-boot:run -Dmaven.test.skip=true
```

4. Run each of the 8 servers with  
```
$ server/mvnw clean spring-boot:run -Dmaven.test.skip=true
```

5. Access the client frontend in your browser at  
```
your-client-address:8082/
```
