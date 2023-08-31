package uk.gov.nationalarchives

import com.dimafeng.testcontainers.DynaliteContainer
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import software.amazon.awssdk.services.dynamodb.model._
import uk.gov.nationalarchives.referencegenerator.Counter
import uk.gov.nationalarchives.utils.TestContainerUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class CounterTest extends AnyFlatSpec with Matchers with TestContainerUtils {
  override def config: Config = ConfigFactory.load()

  override def afterContainersStart(containers: containerDef.Container): Unit = super.afterContainersStart(containers)

  "incrementCounter" should "return an exception if no items are found for the key" in withContainers { case container: DynaliteContainer =>
    val client = createDynamoDbClient(container)
    val config = ConfigFactory
      .load()
      .withValue("dynamodb.key", ConfigValueFactory.fromAnyRef("invalidKey"))

    val counter = new Counter(client, config)
    val response = counter.incrementCounter(1)

    response shouldBe a[Failure[_]]
    response.failed.get shouldBe an[DynamoDbException]
    response.failed.get.getMessage should include("The provided key element does not match the schema")
  }

  "incrementCounter" should "return an exception if the increment is a negative number" in withContainers { case container: DynaliteContainer =>
    val client = createDynamoDbClient(container)
    val counter = new Counter(client, config)
    val response = counter.incrementCounter(-1)

    response shouldBe a[Failure[_]]
    response.failed.get shouldBe an[DynamoDbException]
    response.failed.get.getMessage should include("The conditional request failed")
  }

  "incrementCounter" should "return an exception if the increment is 0" in withContainers { case container: DynaliteContainer =>
    val client = createDynamoDbClient(container)
    val counter = new Counter(client, config)
    val response = counter.incrementCounter(0)

    response shouldBe a[Failure[_]]
    response.failed.get shouldBe an[DynamoDbException]
    response.failed.get.getMessage should include("The conditional request failed")
  }

  "incrementCounter" should "return the current counter if the call to getItem and updateItem succeeds" in withContainers { case container: DynaliteContainer =>
    val client = createDynamoDbClient(container)
    val counter = new Counter(client, config)

    val response = counter.incrementCounter(10)

    response shouldBe Success("10")
  }

  "incrementCounter" should "return an exception if current counter does not match the retrieved counter value" in withContainers { case container: DynaliteContainer =>
    val client = createDynamoDbClient(container)
    val counter = new Counter(client, config)

    val concurrentUpdates = 2
    val futures = (1 to concurrentUpdates).map(_ => Future {
      counter.incrementCounter(1)
    })

    val combinedFuture = Future.sequence(futures)
    val results = Await.result(combinedFuture, 10.seconds).toList.partition(_.isSuccess)

    results._1.head.get should include("20")
    results._2.head.failed.get.getMessage should include("The conditional request failed")
  }
}
