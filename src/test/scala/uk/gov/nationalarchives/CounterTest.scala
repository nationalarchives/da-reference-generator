package uk.gov.nationalarchives

import com.typesafe.config.{Config, ConfigFactory}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model._
import uk.gov.nationalarchives.referencegenerator.Counter

import java.util
import scala.jdk.CollectionConverters.MapHasAsJava
import scala.util.{Failure, Success}

class CounterTest extends AnyFlatSpec with Matchers {
  val config: Config = ConfigFactory.load()

  "incrementCounter" should "return an exception if no items are found for the key" in {
    val mockDDB = mock[DynamoDbClient]
    val getItem: util.Map[String, AttributeValue] = Map.empty[String, AttributeValue].asJava
    val getItemResponse = GetItemResponse.builder().item(getItem).build()

    when(mockDDB.getItem(any[GetItemRequest])).thenReturn(getItemResponse)

    val counter = new Counter(mockDDB, config)
    val response = counter.incrementCounter(1)
    response shouldBe a[Failure[_]]
    response.failed.get shouldBe an[NoSuchElementException]
    response.failed.get.getMessage shouldBe s"""No item found with the key: ${config.getString("dynamodb.key")}"""
  }

  "incrementCounter" should "return an exception if the increment is 0" in {
    val mockDDB = mock[DynamoDbClient]
    val currentCount = "10"
    val getItem = Map("pieceCounter" -> AttributeValue.builder().n(currentCount).build()).asJava
    val getItemResponse = GetItemResponse.builder().item(getItem).build()
    val errorMessage = "The conditional request failed"

    val exception = DynamoDbException.builder().message(errorMessage).build()

    val updateItemRequest = generateUpdateItemRequest(currentCount, numberOfReferences = "0")

    when(mockDDB.getItem(any[GetItemRequest])).thenReturn(getItemResponse)
    when(mockDDB.updateItem(updateItemRequest)).thenThrow(exception)

    val counter = new Counter(mockDDB, config)
    val response = counter.incrementCounter(0)

    response shouldBe a[Failure[_]]
    response.failed.get shouldBe an[DynamoDbException]
    response.failed.get.getMessage shouldBe errorMessage
  }

  "incrementCounter" should "return an exception if the increment is a negative number" in {
    val mockDDB = mock[DynamoDbClient]
    val currentCount = "10"
    val numberOfReferences = "-1"
    val getItem = Map("pieceCounter" -> AttributeValue.builder().n(currentCount).build()).asJava
    val getItemResponse = GetItemResponse.builder().item(getItem).build()
    val errorMessage = "The conditional request failed"

    val exception = DynamoDbException.builder().message(errorMessage).build()

    val updateItemRequest = generateUpdateItemRequest(currentCount, numberOfReferences)

    when(mockDDB.getItem(any[GetItemRequest])).thenReturn(getItemResponse)
    when(mockDDB.updateItem(updateItemRequest)).thenThrow(exception)

    val counter = new Counter(mockDDB, config)
    val response = counter.incrementCounter(-1)

    response shouldBe a[Failure[_]]
    response.failed.get shouldBe an[DynamoDbException]
    response.failed.get.getMessage shouldBe errorMessage
  }

  "incrementCounter" should "return the current counter if the call to getItem and updateItem succeeds" in {
    val mockDDB = mock[DynamoDbClient]
    val getItem = Map("pieceCounter" -> AttributeValue.builder().n("10").build()).asJava
    val getItemResponse = GetItemResponse.builder().item(getItem).build()
    val updateItem = Map("pieceCounter" -> AttributeValue.builder().n("12").build()).asJava
    val updateItemResponse = UpdateItemResponse.builder().attributes(updateItem).build()

    when(mockDDB.getItem(any[GetItemRequest])).thenReturn(getItemResponse)
    when(mockDDB.updateItem(any[UpdateItemRequest])).thenReturn(updateItemResponse)

    val counter = new Counter(mockDDB, config)
    val response = counter.incrementCounter(2)

    response shouldBe Success("10")
  }

  private def generateUpdateItemRequest(currentCounter: String, numberOfReferences: String) = {
    val tableName: String = config.getString("dynamodb.tableName")
    val key: String = config.getString("dynamodb.key")
    val keyVal: String = config.getString("dynamodb.keyVal")
    val pieceCounter: String = config.getString("dynamodb.pieceCounter")
    val keyToGet: util.Map[String, AttributeValue] = Map(key -> AttributeValue.builder().s(keyVal).build()).asJava
    UpdateItemRequest.builder()
      .tableName(tableName)
      .key(keyToGet)
      .updateExpression(s"ADD $pieceCounter :incr")
      .conditionExpression(s"$pieceCounter = :currCounter AND :incr > :zero")
      .expressionAttributeValues(Map(
        ":incr" -> AttributeValue.builder().n(numberOfReferences).build(),
        ":currCounter" -> AttributeValue.builder().n(currentCounter).build(),
        ":zero" -> AttributeValue.builder().n("0").build()
      ).asJava)
      .returnValues(ReturnValue.ALL_NEW)
      .build()
  }
}
