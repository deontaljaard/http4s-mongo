# http4s-mongo
A reference project intended to be a starting point (reference) to use http4s and MongoDB.

Working with the latest technology means that there's not always sufficient documentation for your use case. I started tinkering with http4s and the new async|reactive drivers for MongoDB. I thought I'd share it here for anybody else that is interested in using similar technologies.

# Running the service
### Directly on your host
In the root of the project, invoke the following command:
```bash
sbt service/run
```

### Using docker (assuming it's set up on your host)
In the root of the project, invoke the following commands:
```bash
sbt dockerize
```
followed by
```bash
docker run -d -p 8080:8080 --name http4s deontaljaard.github.io/service
```

# Invoke hello endpoint
### GET to /hello/<name>
```bash
time curl -i http://localhost:8080/api/hello/http4s
```
# Invoke person endpoint
In order for the /person endpoint to work correctly, you require a MongoDB instance running somewhere. I prefer Docker, so if you want to get it working on your local machine, have a look at the Mongo Docker Repository [here](https://hub.docker.com/_/mongo/) and follow the guide. Just ensure the database and collection you configure aligns with the one's expected by the person repositories in the source.

## Example requests to the /person endpoint:
### GET to /persons/<person_id>
```bash
time curl http://localhost:8080/api/persons/<person_id>
```

### POST to /persons
```bash
time curl -i -H "Content-Type: application/json" -X POST -d '{"firstName":"Frodo", "lastName":"Baggins"}' http://localhost:8080/api/persons
```

# Invoke auth endpoint
### POST - Basic auth to /login
```bash
time curl -i -H "Authorization: Basic dGVzdDp0ZXN0" -X POST http://localhost:8080/api/login
```
In the request header, dGVzdDp0ZXN0 is the Base64 encoded version of test:test.

# Invoke files endpoint
In order to upload a file, the service assumes a multipart request with two parts. The first part should contain JSON payload representing metadata accompanying the file to be uploaded. The second part contains the file to upload. The behaviour of this endpoint can be adapted to your use case. For example, it might be overkill to send a JSON payload describing the user id as one of the parts in the multipart request. In that case, simply capture the user id as a path param, like http://localhost:8080/api/files/<person_id>/upload.
### POST to /files/upload
```bash
curl -i \
  -F "metadata={\"userId\": \"4583745\"};type=application/json" \
  -F "file=@/home/deon/Downloads/http4s.png;type=image/png" \
  http://localhost:8080/api/files/upload
```

# Special notes
If you're running the Java 9 JDK, you'll probably encounter a
```java
Caused by: java.lang.ClassNotFoundException: javax.xml.bind.JAXBException
```
when you use the upload utility that communicates with S3. This is because JAXB APIs are no longer contained in the default class path in Java SE 9.

To address this, you can run the service with the following command:
```bash
env JAVA_OPTS="--add-modules=java.xml.bind,java.activation" sbt service/run
```