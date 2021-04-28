## Disributed Matrix Multiplication
This is a RESTful distributed matrix multiplication system utilising gRPC to implement the divide and conquer matrix multiplication algorithm.

### Running the system

1. Adjust the IP addresses of the serevers in client/src/main/java/com/example/grpc/client/grpcclient/GRPCClientService.java  

2. Generate a 2^n matrix with
```
$ generateMatrix.py n
```

3. Run the client with  
```
$ client/mvnw clean spring-boot:run -Dmaven.test.skip=true
```

4. Run the 8 servers with  
```
$ server/mvnw clean spring-boot:run -Dmaven.test.skip=true
```

5. Access the client frontend in your browser at  
```
your-server-address:8082/
```
