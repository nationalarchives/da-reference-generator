package uk.gov.nationalarchives.referencegenerator

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, GetItemRequest, ReturnValue, UpdateItemRequest}

import java.util
import scala.jdk.CollectionConverters.{MapHasAsJava, MapHasAsScala}
import scala.util.Try

class Counter(counterClient: DynamoDbClient) {
  val logger: Logger = Logger[Counter]
  val config: Config = ConfigFactory.load()

  val tableName: String = config.getString("dynamodb.tableName")
  val key: String = config.getString("dynamodb.key")
  val keyVal: String = config.getString("dynamodb.keyVal")
  val pieceCounter: String = config.getString("dynamodb.pieceCounter")
  val keyToGet: util.Map[String, AttributeValue] = Map(key -> AttributeValue.builder().s(keyVal).build()).asJava

  val updateExpression = s"ADD $pieceCounter :incr"
  val conditionExpression = s"$pieceCounter = :currCounter AND :incr > :zero"
  val createExpressionAttributeValues: (String, Int) => java.util.Map[String, AttributeValue] = (currentCounter, numberOfReferences) => {
    Map(
      ":incr" -> AttributeValue.builder().n(numberOfReferences.toString).build(),
      ":currCounter" -> AttributeValue.builder().n(currentCounter).build(),
      ":zero" -> AttributeValue.builder().n("0").build()
    ).asJava
  }

  val currentCountRequest: GetItemRequest = GetItemRequest.builder()
    .key(keyToGet)
    .tableName(tableName)
    .build()


  val incrementCounterRequest: (String, Int) => UpdateItemRequest = (currentCounter, numberOfReferences) => UpdateItemRequest.builder()
    .tableName(tableName)
    .key(keyToGet)
    .updateExpression(updateExpression)
    .conditionExpression(conditionExpression)
    .expressionAttributeValues(createExpressionAttributeValues(currentCounter, numberOfReferences))
    .returnValues(ReturnValue.ALL_NEW)
    .build()

  def incrementCounter(numberOfReferences: Int): Try[String] = {
    Try {
      val currentCounterResponse: Map[String, AttributeValue] = counterClient.getItem(currentCountRequest).item.asScala.toMap

      if (currentCounterResponse.nonEmpty) {
        val currentCounter = currentCounterResponse(pieceCounter).n()
        counterClient.updateItem(incrementCounterRequest(currentCounter, numberOfReferences))
        logger.info(s"The counter value $currentCounter has been updated by $numberOfReferences")
        currentCounter
      } else {
        throw new NoSuchElementException(s"No item found with the key: $key")
      }
    }
  }
}
