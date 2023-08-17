package uk.gov.nationalarchives.referencegenerator

import com.typesafe.scalalogging.Logger
import io.circe.syntax._
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, GetItemRequest, ReturnValue, UpdateItemRequest}
import uk.gov.nationalarchives.referencegenerator.Reference._

import java.util
import scala.jdk.CollectionConverters.{MapHasAsJava, MapHasAsScala}
import scala.util.Try

class DynamoDb {
  val logger: Logger = Logger[DynamoDb]
  val ddb: DynamoDbClient = DynamoDbClient.builder()
    .credentialsProvider(DefaultCredentialsProvider.create())
    .region(Region.EU_WEST_2)
    .build()
  val tableName = "da-reference-counter"
  val key: String = "v1"
  val keyVal: String = "filePieceCounter"
  val pieceCounter = "pieceCounter"
  val keyToGet: util.Map[String, AttributeValue] = Map(key -> AttributeValue.builder().s(keyVal).build()).asJava

  val request: GetItemRequest = GetItemRequest.builder()
    .key(keyToGet)
    .tableName(tableName)
    .build()

  def getItem: Try[String] = {
    Try {
      val returnedItem: Map[String, AttributeValue] = ddb.getItem(request).item.asScala.toMap
      if (returnedItem.nonEmpty) {
        val keys = returnedItem
        logger.info(s"The current counter is ${keys(pieceCounter).n()}")
        keys(pieceCounter).n()
      } else {
        throw new NoSuchElementException(s"No item found with the key: $key")
      }
    }
  }

  def updateTableItem(currentCounter: String, increment: Int): Try[String] = {
    val request: UpdateItemRequest = UpdateItemRequest.builder()
      .tableName(tableName)
      .key(keyToGet)
      .updateExpression(s"ADD $pieceCounter :incr")
      .conditionExpression(s"$pieceCounter = :currCounter AND :incr > :zero")
      .expressionAttributeValues(Map(
        ":incr" -> AttributeValue.builder().n(increment.toString).build(),
        ":currCounter" -> AttributeValue.builder().n(currentCounter).build(),
        ":zero" -> AttributeValue.builder().n("0").build()
      ).asJava)
      .returnValues(ReturnValue.ALL_NEW)
      .build()

    Try {
      val response = ddb.updateItem(request)
      val encryptedReferences = generateReferences(currentCounter.toInt, increment).asJson.noSpaces
      logger.info(s"The counter has been updated by $increment")
      logger.info(s"The current counter is now ${response.attributes().get(pieceCounter).n()}")
      encryptedReferences
    }
  }

  private def generateReferences(currentCount: Int, count: Int): List[EncryptedReference] = {
    (currentCount until currentCount + count).map(cc => EncryptedReference(Base25Encoder.encode(cc.toLong))).toList
  }
}
