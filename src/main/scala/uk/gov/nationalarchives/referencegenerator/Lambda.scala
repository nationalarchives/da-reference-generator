package uk.gov.nationalarchives.referencegenerator

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import uk.gov.nationalarchives.referencegenerator.Lambda.{PieceReference, Input}
import io.circe.syntax._

import scala.util.{Failure, Success}

class Lambda(counterClient: DynamoDbClient, config: Config) extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {
  val logger: Logger = Logger[Lambda]

  override def handleRequest(event: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    val queryParams = event.getQueryStringParameters
    val queryParam: String = config.getString("dynamodb.queryParam")
    val numberOfRefs = queryParams.get(queryParam).toInt

    process(Input(numberOfRefs))
  }

  def process(input: Input): APIGatewayProxyResponseEvent = {
    val counter = new Counter(counterClient, config)
    val response = new APIGatewayProxyResponseEvent()
    counter.incrementCounter(input.numberOfReferences) match {
      case Success(currentCounter) =>
        val references = generateReferences(currentCounter.toInt, input.numberOfReferences).asJson.noSpaces
        logger.info(s"Generated the following references: $references")
        response.setStatusCode(200)
        response.setBody(references)
        response
      case Failure(exception) =>
        logger.error(exception.getMessage)
        response.setStatusCode(500)
        response.setBody(exception.getMessage)
        response
    }
  }

  private def generateReferences(currentCounter: Int, count: Int): List[String] = {
    (currentCounter until currentCounter + count).map(cc => PieceReference(Base25Encoder.encode(cc.toLong)).reference).toList
  }
}

object Lambda {

  val counterClient: DynamoDbClient = DynamoDbClient.builder()
    .credentialsProvider(DefaultCredentialsProvider.create())
    .region(Region.EU_WEST_2)
    .build()

  val config: Config = ConfigFactory.load()

  case class Input(numberOfReferences: Int)

  case class PieceReference(reference: String)

  def apply(): Lambda = new Lambda(counterClient, config)
}
