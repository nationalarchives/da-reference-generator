package uk.gov.nationalarchives.referencegenerator

import com.typesafe.scalalogging.Logger
import uk.gov.nationalarchives.referencegenerator.Lambda.Input

import scala.util.{Failure, Success}

class Lambda {
  val logger: Logger = Logger[Lambda]

  def process(input: Input): Unit = {
    val dynamoDb = new DynamoDb
    dynamoDb.getItem match {
      case Success(value) =>
        dynamoDb.updateTableItem(value, 1) match {
          case Success(encryptedReferences) => logger.info(s"Generated the following references: $encryptedReferences")
          case Failure(errorMsg) => logger.error(errorMsg.getMessage)
        }
      case Failure(errorMsg) => logger.error(errorMsg.getMessage)
    }
  }
}

object Lambda {
  case class Input(numberOfReferences: Int)
}
