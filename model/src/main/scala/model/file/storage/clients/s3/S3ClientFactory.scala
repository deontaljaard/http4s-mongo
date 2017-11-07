package model.file.storage.clients.s3

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}

object S3ClientFactory {

  lazy private val basicAWSCredentials = new BasicAWSCredentials("AKIAIFWITYNZ27R3ZZJA", "8XYyQKFl5xK0PhjxUgRudfvjnVXuq9OFtKaGx2ua")

  lazy private val awsStaticCredentialsProvider = new AWSStaticCredentialsProvider(basicAWSCredentials)

  lazy val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard()
    .withRegion(Regions.EU_WEST_1)
    .withCredentials(awsStaticCredentialsProvider)
    .build()

  lazy val s3Bucket: String = "bucket-name"
}
