# http4s-mongo
A reference project intended to be a starting point (reference) to use http4s and MongoDB.

Working with the latest technology means that there's not always sufficient documentation for your use case. I started tinkering with http4s and the new async|reactive drivers for MongoDB. I thought I'd share it here for anybody else that is interested in using similar technologies.

# Running the server
In the root of the project, invoke the following command:
```bash
sbt service/run
```

# Invoke hello endpoint
```bash
time curl -i http://localhost:8080/api/hello/http4s
```
In order for the /person endpoint to work correctly, you require a MongoDB instance running somewhere. I prefer Docker, so if you want to get it working on your local machine, have a look at the Mongo Docker Repository [here](https://hub.docker.com/_/mongo/) and follow the guide. Just ensure the database and collection you configure aligns with the one's expected by the person repositories in the source.
