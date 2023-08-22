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

  val request: GetItemRequest = GetItemRequest.builder()
    .key(keyToGet)
    .tableName(tableName)
    .build()

  def currentCounter: Try[String] = {
    Try {
      val returnedItem: Map[String, AttributeValue] = counterClient.getItem(request).item.asScala.toMap
      if (returnedItem.nonEmpty) {
        val keys = returnedItem
        logger.info(s"The current counter is ${keys(pieceCounter).n()}")
        keys(pieceCounter).n()
      } else {
        throw new NoSuchElementException(s"No item found with the key: $key")
      }
    }
  }

  def incrementCounter(currentCounter: String, numberOfReferences: Int): Try[(String, Int)] = {
    val request: UpdateItemRequest = UpdateItemRequest.builder()
      .tableName(tableName)
      .key(keyToGet)
      .updateExpression(updateExpression)
      .conditionExpression(conditionExpression)
      .expressionAttributeValues(createExpressionAttributeValues(currentCounter, numberOfReferences))
      .returnValues(ReturnValue.ALL_NEW)
      .build()

    Try {
      val response = counterClient.updateItem(request)
      logger.info(s"The counter has been updated by $numberOfReferences")
      logger.info(s"The current counter is now ${response.attributes().get(pieceCounter).n()}")
      (currentCounter, numberOfReferences)
    }
  }
}
