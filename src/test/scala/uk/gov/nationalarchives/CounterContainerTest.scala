package uk.gov.nationalarchives

import com.dimafeng.testcontainers.DynaliteContainer
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import software.amazon.awssdk.services.dynamodb.model._
import uk.gov.nationalarchives.referencegenerator.Counter
import uk.gov.nationalarchives.utils.TestContainerUtils

import scala.util.{Failure, Success}

class CounterContainerTest extends AnyFlatSpec with Matchers with TestContainerUtils {
  override def config: Config = ConfigFactory.load()

  override def afterContainersStart(containers: containerDef.Container): Unit = super.afterContainersStart(containers)

  "incrementCounter" should "return an exception if no items are found for the key" in withContainers { case container: DynaliteContainer =>
    val client = createDynamoDbClient(container)
    val counter = new Counter(client, config)

    val response = counter.incrementCounter(1)

    response shouldBe a[Failure[_]]
    response.failed.get shouldBe an[DynamoDbException]
    response.failed.get.getMessage should include("The provided key element does not match the schema")
  }

  "incrementCounter" should "return the current counter if the call to getItem and updateItem succeeds" in withContainers { case container: DynaliteContainer =>
    val client = createDynamoDbClient(container)
    val counter = new Counter(client, config)

    val response = counter.incrementCounter(2)

    response shouldBe Success("10")
  }
}
