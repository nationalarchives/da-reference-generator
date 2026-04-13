package uk.gov.nationalarchives.utils

import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.dimafeng.testcontainers.{ContainerDef, LocalStackV2Container}
import com.typesafe.config.Config
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model._
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter

import java.net.URI
import scala.jdk.CollectionConverters._

trait TestContainerUtils extends AnyFlatSpec with TestContainerForAll with BeforeAndAfterEach {
  val tableName: String = config.getString("dynamodb.tableName")
  val primaryKey: String = config.getString("dynamodb.key")
  val counterColumn: String = config.getString("dynamodb.referenceCounter")
  val fileCounter: String = config.getString("dynamodb.keyVal")

  val attributeDefinitions: List[AttributeDefinition] = List(
    AttributeDefinition.builder()
      .attributeName(primaryKey)
      .attributeType(ScalarAttributeType.S)
      .build()
  )
  val keySchema: List[KeySchemaElement] = List(
    KeySchemaElement.builder()
      .attributeName(primaryKey)
      .keyType(KeyType.HASH)
      .build()
  )
  val createTableRequest: CreateTableRequest = CreateTableRequest.builder()
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

  def config: Config

  override val containerDef: ContainerDef = LocalStackV2Container.Def()

  override def afterContainersStart(containers: containerDef.Container): Unit = {
    super.afterContainersStart(containers)
    containers match {
      case container: LocalStackV2Container => createTable(container)
    }
  }

  protected def createDynamoDbClient(container: LocalStackV2Container): DynamoDbClient = {
    val endpoint = URI.create(s"http://${container.container.getHost}:${container.container.getMappedPort(4566)}")
    DynamoDbClient.builder()
      .credentialsProvider(container.staticCredentialsProvider)
      .region(container.region)
      .endpointOverride(endpoint)
      .build()
  }

  private def createTable(container: LocalStackV2Container): Unit = {
    val client: DynamoDbClient = createDynamoDbClient(container)

    client.createTable(createTableRequest)

    val waiter = DynamoDbWaiter.builder()
      .client(client)
      .build()

    waiter.waitUntilTableExists(
      DescribeTableRequest.builder().tableName(tableName).build()
    )

    val putItemRequest = PutItemRequest.builder()
      .tableName(tableName)
      .item(
        Map(
          primaryKey -> AttributeValue.builder().s(fileCounter).build(),
          counterColumn -> AttributeValue.builder().n("10").build()
        ).asJava
      )
      .build()

    client.putItem(putItemRequest)
  }
}
