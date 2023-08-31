package uk.gov.nationalarchives

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.dimafeng.testcontainers.DynaliteContainer
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.nationalarchives.referencegenerator.Lambda
import uk.gov.nationalarchives.utils.TestContainerUtils

class LambdaTest extends AnyFlatSpec with Matchers with TestContainerUtils {
  override def config: Config = ConfigFactory.load()

  override def afterContainersStart(containers: containerDef.Container): Unit = super.afterContainersStart(containers)

  "The Lambda class" should "return an APIGateWayResponseEvent with the correct number of references" in withContainers { case container: DynaliteContainer =>
    val client = createDynamoDbClient(container)
    val input = Lambda.Input(numberOfReferences = 3)

    val lambda = new Lambda(client, config)
    val actual: APIGatewayProxyResponseEvent = lambda.process(input)
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

    val lambda = new Lambda(client, config)
    val actual: APIGatewayProxyResponseEvent = lambda.process(input)
    val expected: APIGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent()
      .withStatusCode(500)
      .withBody("The provided key element does not match the schema")
    actual.getStatusCode shouldBe expected.getStatusCode
    actual.getBody should include(expected.getBody)
  }
}
