package uk.gov.nationalarchives

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model._
import uk.gov.nationalarchives.referencegenerator.Lambda

import scala.jdk.CollectionConverters.MapHasAsJava

class LambdaTest extends AnyFlatSpec with Matchers {

  "The Lambda class" should "successfully process a request with the correct number of references" in {
    val mockDDB = mock[DynamoDbClient]
    val input = Lambda.Input(numberOfReferences = 3)
    val lambda = new Lambda()
    val getItemMap = Map("pieceCounter" -> AttributeValue.builder().n("10").build()).asJava
    val getItemResponse = GetItemResponse.builder().item(getItemMap).build()

    when(mockDDB.getItem(any[GetItemRequest])).thenReturn(getItemResponse)

    val updateItemMap = Map("pieceCounter" -> AttributeValue.builder().n("12").build()).asJava
    val updateItemResponse = UpdateItemResponse.builder().attributes(updateItemMap).build()

    when(mockDDB.updateItem(any[UpdateItemRequest])).thenReturn(updateItemResponse)

    val actual: APIGatewayProxyResponseEvent = lambda.process(input, mockDDB)
    val expected: APIGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent()
      .withStatusCode(200)
      .withBody("""["N","P","Q"]""")
    actual shouldBe expected
  }

  it should "return an exception message if getItem fails" in {
    val mockDDB = mock[DynamoDbClient]
    val input = Lambda.Input(numberOfReferences = 1)
    val lambda = new Lambda()
    val noSuchElementException = new NoSuchElementException("No item found")

    when(mockDDB.getItem(any[GetItemRequest])).thenThrow(noSuchElementException)

    val updateItemMap = Map("pieceCounter" -> AttributeValue.builder().n("12").build()).asJava
    val updateItemResponse = UpdateItemResponse.builder().attributes(updateItemMap).build()

    when(mockDDB.updateItem(any[UpdateItemRequest])).thenReturn(updateItemResponse)

    val actual: APIGatewayProxyResponseEvent = lambda.process(input, mockDDB)
    val expected: APIGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent()
      .withStatusCode(500)
      .withBody("No item found")
    actual shouldBe expected
  }

  it should "return an exception message if updateItem fails" in {
    val mockDDB = mock[DynamoDbClient]
    val input = Lambda.Input(numberOfReferences = 1)
    val lambda = new Lambda()
    val getItemMap = Map("pieceCounter" -> AttributeValue.builder().n("10").build()).asJava
    val getItemResponse = GetItemResponse.builder().item(getItemMap).build()

    when(mockDDB.getItem(any[GetItemRequest])).thenReturn(getItemResponse)

    val resourceNotFoundException = ResourceNotFoundException.builder().message("The increment cannot be zero").build()

    when(mockDDB.updateItem(any[UpdateItemRequest])).thenThrow(resourceNotFoundException)

    val actual: APIGatewayProxyResponseEvent = lambda.process(input, mockDDB)
    val expected: APIGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent()
      .withStatusCode(500)
      .withBody("The increment cannot be zero")
    actual shouldBe expected
  }
}
