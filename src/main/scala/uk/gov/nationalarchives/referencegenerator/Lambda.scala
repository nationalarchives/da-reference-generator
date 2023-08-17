package uk.gov.nationalarchives.referencegenerator

import com.typesafe.scalalogging.Logger
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import uk.gov.nationalarchives.referencegenerator.Lambda.Input

import scala.util.{Failure, Success}

class Lambda {
  val logger: Logger = Logger[Lambda]

  def process(input: Input): Unit = {
    val client: DynamoDbClient = DynamoDbClient.builder()
      .credentialsProvider(DefaultCredentialsProvider.create())
      .region(Region.EU_WEST_2)
      .build()
    val dynamoDb = new DynamoDb(client)
    dynamoDb.getCount match {
      case Success(value) =>
        dynamoDb.getReferences(value, input.numberOfReferences) match {
          case Success(encryptedReferences) => logger.info(s"Generated the following references: $encryptedReferences")
          case Failure(errorMsg) => logger.error(errorMsg.getMessage)
        }
      case Failure(errorMsg) => logger.error(errorMsg.getMessage)
    }
  }
}

object Lambda {
  case class Input(numberOfReferences: Int)
}
