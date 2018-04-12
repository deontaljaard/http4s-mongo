# http4s-mongo
A reference project intended to be a starting point (reference) to use http4s and MongoDB.

Note: If you're upgrading the http4s version, the [changelog](https://github.com/http4s/http4s/blob/master/website/src/hugo/content/changelog.md) is invaluable.

Working with the latest technology means that there's not always sufficient documentation for your use case. I started tinkering with http4s and the new async|reactive drivers for MongoDB. I thought I'd share it here for anybody else that is interested in using similar technologies.

# Running the service
### Directly on your host
In the root of the project, invoke the following command:
```bash
sbt service/run
```

### Using docker (assuming it's set up on your host)
In the root of the project, invoke the following:
```bash
sbt dockerize
```
If you'd like to run a MongoDB instance, run the following:
```bash
docker run --name mongo -p 27017:27017 -d mongo
```
This should pull and run the latest mongo image.

Add the required DB and collection.

To start the http4s-mongo service container, run the following:
```bash
docker run --name http4s -d -p 8080:8080 --link mongo:mongo deontaljaard.github.io/service
```
This will link the mongo container to the http4s container. Note that the connection string changes in this case as a result of the --link flag. The connection string to mongo will change from:
```scala
mongodb://[user:pass@]localhost:27017
```
to this:
```scala
mongodb://[user:pass@]mongo:27017
```

# Invoke hello endpoint
### GET to /hello/<name>
```bash
time curl -i http://localhost:8080/api/hello/http4s
```
# Invoke person endpoint
In order for the /person endpoint to work correctly, you require a MongoDB instance running somewhere. You can set this up quickly by referring to the above.

## Example requests to the /person endpoint:
### GET to /persons/<person_id>
```bash
time curl http://localhost:8080/api/persons/<person_id>
```

### POST to /persons
```bash
time curl -i -H "Content-Type: application/json" -X POST -d '{"firstName":"Frodo", "lastName":"Baggins"}' http://localhost:8082/api/persons
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
