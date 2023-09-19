package uk.gov.nationalarchives.referencegenerator

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import uk.gov.nationalarchives.referencegenerator.Lambda.{Input, Reference, config, counterClient, logger}
import io.circe.syntax._
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.apache.ApacheHttpClient

import scala.util.{Failure, Success, Try}

class Lambda extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {

  override def handleRequest(event: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    val limit = config.getString("dynamodb.referenceLimit").toInt
    val queryParams = event.getQueryStringParameters
    val queryParam: String = config.getString("dynamodb.queryParam")
    val numberOfRefsQuery: Try[Int] = Try {
      val numberOfRefs = queryParams.get(queryParam).toInt
      if (numberOfRefs > limit) throw new IllegalArgumentException(s"$queryParam is greater than $limit") else numberOfRefs
    }
    numberOfRefsQuery match {
      case Success(numberOfReferences) =>
        process(Input(numberOfReferences))
      case Failure(exception) =>
        val response = new APIGatewayProxyResponseEvent()
        logger.error(exception.toString)
        response.setStatusCode(500)
        response.setBody(exception.getMessage)
        response
    }
  }

  def process(input: Input, counterClient: DynamoDbClient = counterClient, config: Config = config): APIGatewayProxyResponseEvent = {
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
    (currentCounter until currentCounter + count).map(cc => Reference(Encoder.encode(cc.toLong)).reference).toList
  }
}

object Lambda {
  val logger: Logger = Logger[Lambda]
  val sdkHttpClient: SdkHttpClient = ApacheHttpClient.builder().build()
  val counterClient: DynamoDbClient = DynamoDbClient.builder()
    .credentialsProvider(DefaultCredentialsProvider.create())
    .httpClient(sdkHttpClient)
    .region(Region.EU_WEST_2)
    .build()

  val config: Config = ConfigFactory.load()

  case class Input(numberOfReferences: Int)

  case class Reference(reference: String)
}
