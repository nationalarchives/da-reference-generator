package uk.gov.nationalarchives.utils


import com.dimafeng.testcontainers.scalatest.{TestContainerForAll, TestContainerForEach}
import com.dimafeng.testcontainers.{ContainerDef, DynaliteContainer}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeDefinition, AttributeValue, CreateTableRequest, DescribeTableRequest, KeySchemaElement, KeyType, ProvisionedThroughput, PutItemRequest, ScalarAttributeType}
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter

import scala.jdk.CollectionConverters._
import java.net.URI

trait TestContainerUtils extends AnyFlatSpec with TestContainerForAll with BeforeAndAfterEach {
  val tableName: String = config.getString("dynamodb.tableName")
  val primaryKey: String = config.getString("dynamodb.key")
  val pieceCounterColumn: String = config.getString("dynamodb.pieceCounter")
  val filePieceCounter: String = config.getString("dynamodb.keyVal")

  def config: Config

  override val containerDef: ContainerDef = DynaliteContainer.Def(
    dockerImageName = DockerImageName.parse("quay.io/testcontainers/dynalite:v1.2.1-1")
  )

  override def afterContainersStart(containers: containerDef.Container): Unit = {
    super.afterContainersStart(containers)
    containers match {
      case container: DynaliteContainer => createTable(container)
    }
  }

  protected def createDynamoDbClient(container: DynaliteContainer): DynamoDbClient = {
    DynamoDbClient.builder()
      .credentialsProvider(DefaultCredentialsProvider.create())
      .region(Region.EU_WEST_2)
      .endpointOverride(URI.create(container.endpointConfiguration.getServiceEndpoint))
      .build()
  }

  private def createTable(container: DynaliteContainer): Unit = {
    val client: DynamoDbClient = DynamoDbClient.builder()
      .credentialsProvider(DefaultCredentialsProvider.create())
      .region(Region.EU_WEST_2)
      .endpointOverride(URI.create(container.endpointConfiguration.getServiceEndpoint))
      .build()

    val attributeDefinitions = List(
      AttributeDefinition.builder()
        .attributeName(primaryKey)
        .attributeType(ScalarAttributeType.S) // String type
        .build()
    )

    val keySchema = List(
      KeySchemaElement.builder()
        .attributeName(primaryKey)
        .keyType(KeyType.HASH)
        .build()
    )

    val createTableRequest = CreateTableRequest.builder()
      .tableName(tableName)
      .attributeDefinitions(attributeDefinitions.asJava)
      .keySchema(keySchema.asJava)
      .provisionedThroughput(
        ProvisionedThroughput.builder()
          .readCapacityUnits(5L)
          .writeCapacityUnits(5L)
          .build()
      )
      .build()

    // Create the table
    client.createTable(createTableRequest)
    println(s"Table created: $tableName")

    val waiter = DynamoDbWaiter.builder()
      .client(client)
      .build()

    waiter.waitUntilTableExists(
      DescribeTableRequest.builder().tableName(tableName).build()
    )

    // Put an entry into the table
    val putItemRequest = PutItemRequest.builder()
      .tableName(tableName)
      .item(
        Map(
          primaryKey -> AttributeValue.builder().s(filePieceCounter).build(),
          pieceCounterColumn -> AttributeValue.builder().n("10").build()
        ).asJava
      )
      .build()

    client.putItem(putItemRequest)
    println("Item added to the table.")
  }
}
