package uk.gov.nationalarchives

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model._
import uk.gov.nationalarchives.referencegenerator.Counter

import scala.jdk.CollectionConverters.MapHasAsJava
import scala.util.{Failure, Success}

class CounterTest extends AnyFlatSpec with Matchers {

  "currentCounter" should "return the current counter" in {
    val mockDDB = mock[DynamoDbClient]
    val item = Map("pieceCounter" -> AttributeValue.builder().n("10").build()).asJava
    val getItemResponse = GetItemResponse.builder().item(item).build()

    when(mockDDB.getItem(any[GetItemRequest])).thenReturn(getItemResponse)

    val dynamoDb = new Counter(mockDDB)
    val currentCounter = dynamoDb.currentCounter

    currentCounter shouldBe Success("10")
  }

  "currentCounter" should "throw an exception if no item is found" in {
    val mockDDB = mock[DynamoDbClient]
    val noSuchElementException = new NoSuchElementException("No item found")

    when(mockDDB.getItem(any[GetItemRequest])).thenThrow(noSuchElementException)

    val dynamoDb = new Counter(mockDDB)
    val caughtException = dynamoDb.currentCounter

    caughtException shouldBe Failure(noSuchElementException)
  }

  "incrementCounter" should "return the currentCounter and numberOfReferences if updateItem succeeds" in {
    val mockDDB = mock[DynamoDbClient]

    val item = Map("pieceCounter" -> AttributeValue.builder().n("12").build()).asJava
    val updateItemResponse = UpdateItemResponse.builder().attributes(item).build()

    when(mockDDB.updateItem(any[UpdateItemRequest])).thenReturn(updateItemResponse)

    val dynamoDb = new Counter(mockDDB)
    val encryptedReferences = dynamoDb.incrementCounter("10", 2)

    encryptedReferences shouldBe Success(("10", 2))
  }

  "incrementCounter" should "throw an exception if updateItem fails" in {
    val mockDDB = mock[DynamoDbClient]
    val resourceNotFoundException = ResourceNotFoundException.builder().message("The increment cannot be zero").build()

    when(mockDDB.updateItem(any[UpdateItemRequest])).thenThrow(resourceNotFoundException)

    val dynamoDb = new Counter(mockDDB)
    val caughtException = dynamoDb.incrementCounter("10", 0)

    caughtException shouldBe Failure(resourceNotFoundException)
  }
}
