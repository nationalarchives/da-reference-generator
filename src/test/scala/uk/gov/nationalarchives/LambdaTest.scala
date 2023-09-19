package uk.gov.nationalarchives

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{ClientContext, CognitoIdentity, Context, LambdaLogger}
import com.dimafeng.testcontainers.DynaliteContainer
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.nationalarchives.referencegenerator.Lambda
import uk.gov.nationalarchives.utils.TestContainerUtils

import scala.jdk.CollectionConverters.MapHasAsJava

class LambdaTest extends AnyFlatSpec with Matchers with TestContainerUtils {
  override def config: Config = ConfigFactory.load()

  override def afterContainersStart(containers: containerDef.Container): Unit = super.afterContainersStart(containers)

  val mockContext: Context = new Context {
    override def getAwsRequestId: String = "testRequestId"
    override def getLogGroupName: String = "testLogGroupName"
    override def getLogStreamName: String = "testLogStreamName"
    override def getFunctionName: String = "testFunctionName"
    override def getFunctionVersion: String = "testFunctionVersion"
    override def getInvokedFunctionArn: String = "testInvokedFunctionArn"
    override def getIdentity: CognitoIdentity = ???
    override def getClientContext: ClientContext = ???
    override def getRemainingTimeInMillis: Int = ???
    override def getMemoryLimitInMB: Int = ???
    override def getLogger: LambdaLogger = ???
  }

  "The Lambda class" should "return an APIGateWayResponseEvent with the correct number of references" in withContainers { case container: DynaliteContainer =>
    val client = createDynamoDbClient(container)
    val input = Lambda.Input(numberOfReferences = 3)

    val lambda = new Lambda()
    val actual: APIGatewayProxyResponseEvent = lambda.process(input, client, config)
    val expected: APIGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent()
      .withStatusCode(200)
      .withBody("""["N","P","Q"]""")
    actual shouldBe expected
  }

  "The Lambda class" should "return an APIGateWayResponseEvent with body containing exception message if dynamoDB calls fail" in withContainers { case container: DynaliteContainer =>
    val client = createDynamoDbClient(container)
    val input = Lambda.Input(numberOfReferences = 3)
    val config = ConfigFactory
      .load()
      .withValue("dynamodb.key", ConfigValueFactory.fromAnyRef("invalidKey"))

    val lambda = new Lambda()
    val actual: APIGatewayProxyResponseEvent = lambda.process(input, client, config)
    val expected: APIGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent()
      .withStatusCode(500)
      .withBody("The provided key element does not match the schema")
    actual.getStatusCode shouldBe expected.getStatusCode
    actual.getBody should include(expected.getBody)
  }

  "The Lambda class" should "return an APIGateWayResponseEvent with body containing exception message if numberofrefs isn't an integer" in {
    val lambda = new Lambda()
    val queryParam = config.getString("dynamodb.queryParam")
    val queryParams = Map(queryParam -> "abc").asJava
    val event = new APIGatewayProxyRequestEvent()
    event.setQueryStringParameters(queryParams)

    val result = lambda.handleRequest(event, mockContext)
    val expected: APIGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent()
      .withStatusCode(500)
      .withBody(s"""For input string: "abc"""")
    result shouldBe expected
  }

  "The Lambda class" should "return an APIGateWayResponseEvent with body containing exception message if numberofrefs exceeds the limit" in {
    val lambda = new Lambda()
    val queryParam = config.getString("dynamodb.queryParam")
    val queryParams = Map(queryParam -> "5001").asJava
    val event = new APIGatewayProxyRequestEvent()
    event.setQueryStringParameters(queryParams)

    val result = lambda.handleRequest(event, mockContext)
    val expected: APIGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent()
      .withStatusCode(500)
      .withBody(s"""$queryParam is greater than 5000""")
    result shouldBe expected
  }
}
