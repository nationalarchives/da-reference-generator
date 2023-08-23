package uk.gov.nationalarchives.referencegenerator

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import uk.gov.nationalarchives.referencegenerator.Lambda.{EncryptedReference, Input}
import io.circe.syntax._

import scala.util.{Failure, Success}

class Lambda extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {
  val logger: Logger = Logger[Lambda]
  val client: DynamoDbClient = DynamoDbClient.builder()
    .credentialsProvider(DefaultCredentialsProvider.create())
    .region(Region.EU_WEST_2)
    .build()

  override def handleRequest(event: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    val config: Config = ConfigFactory.load()
    val queryParams = event.getQueryStringParameters
    val queryParam: String = config.getString("dynamodb.queryParam")
    val numberOfRefs = queryParams.get(queryParam).toInt

    process(Input(numberOfRefs), client)
  }

  def process(input: Input, client: DynamoDbClient = client): APIGatewayProxyResponseEvent = {
    val counter = new Counter(client)
    val response = new APIGatewayProxyResponseEvent()
    counter.incrementCounter(input.numberOfReferences) match {
      case Success(currentCounter) =>
        val encryptedReferences = generateReferences(currentCounter.toInt, input.numberOfReferences).asJson.noSpaces
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

  private def generateReferences(currentCount: Int, count: Int): List[String] = {
    (currentCount until currentCount + count).map(cc => EncryptedReference(Base25Encoder.encode(cc.toLong)).reference).toList
  }
}

object Lambda {
  case class Input(numberOfReferences: Int)

  case class EncryptedReference(reference: String)
}
