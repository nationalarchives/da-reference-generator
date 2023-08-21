package uk.gov.nationalarchives.referencegenerator

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import uk.gov.nationalarchives.referencegenerator.Lambda.Input

import scala.util.{Failure, Success}

class Lambda extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {
  val logger: Logger = Logger[Lambda]

  override def handleRequest(event: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    val config: Config = ConfigFactory.load()
    val queryParams = event.getQueryStringParameters
    val queryParam: String = config.getString("queryParam")
    val numberOfRefs = queryParams.get(queryParam).toInt

    val client: DynamoDbClient = DynamoDbClient.builder()
      .credentialsProvider(DefaultCredentialsProvider.create())
      .region(Region.EU_WEST_2)
      .build()

    process(Input(numberOfRefs), client)
  }

  def process(input: Input, client: DynamoDbClient): APIGatewayProxyResponseEvent = {
    val dynamoDb = new DynamoDb(client)
    val getCurrentCount = dynamoDb.getCount
    val getReferences = getCurrentCount.flatMap(currentCount => dynamoDb.getReferences(currentCount, input.numberOfReferences))
    val response = new APIGatewayProxyResponseEvent()
    getReferences match {
      case Success(encryptedReferences) =>
        logger.info(s"Generated the following references: $encryptedReferences")
        response.setStatusCode(200)
        response.setBody(encryptedReferences)
        response
      case Failure(exception) =>
        logger.error(exception.getMessage)
        response.setStatusCode(500)
        response.setBody(exception.getMessage)
        response
    }
  }
}

object Lambda {
  case class Input(numberOfReferences: Int)
}
