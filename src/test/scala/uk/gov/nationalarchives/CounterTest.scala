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

  "incrementCounter" should "throw an exception if no item is found" in {
    val mockDDB = mock[DynamoDbClient]
    val noSuchElementException = new NoSuchElementException("No item found")

    when(mockDDB.getItem(any[GetItemRequest])).thenThrow(noSuchElementException)

    val counter = new Counter(mockDDB)
    val caughtException = counter.incrementCounter(2)

    caughtException shouldBe Failure(noSuchElementException)
  }

  "incrementCounter" should "return the current counter if the call to getItem and updateItem succeeds" in {
    val mockDDB = mock[DynamoDbClient]
    val getItem = Map("pieceCounter" -> AttributeValue.builder().n("10").build()).asJava
    val getItemResponse = GetItemResponse.builder().item(getItem).build()
    val updateItem = Map("pieceCounter" -> AttributeValue.builder().n("12").build()).asJava
    val updateItemResponse = UpdateItemResponse.builder().attributes(updateItem).build()

    when(mockDDB.getItem(any[GetItemRequest])).thenReturn(getItemResponse)
    when(mockDDB.updateItem(any[UpdateItemRequest])).thenReturn(updateItemResponse)

    val counter = new Counter(mockDDB)
    val encryptedReferences = counter.incrementCounter(2)

    encryptedReferences shouldBe Success("10")
  }

  "incrementCounter" should "throw an exception if getItem succeeds but updateItem fails" in {
    val mockDDB = mock[DynamoDbClient]
    val getItem = Map("pieceCounter" -> AttributeValue.builder().n("10").build()).asJava
    val getItemResponse = GetItemResponse.builder().item(getItem).build()
    val resourceNotFoundException = ResourceNotFoundException.builder().message("The increment cannot be zero").build()

    when(mockDDB.getItem(any[GetItemRequest])).thenReturn(getItemResponse)
    when(mockDDB.updateItem(any[UpdateItemRequest])).thenThrow(resourceNotFoundException)

    val counter = new Counter(mockDDB)
    val caughtException = counter.incrementCounter(0)

    caughtException shouldBe Failure(resourceNotFoundException)
  }
}
