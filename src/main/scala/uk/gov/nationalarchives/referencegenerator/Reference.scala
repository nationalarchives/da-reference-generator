package uk.gov.nationalarchives.referencegenerator

import io.circe._
import io.circe.generic.semiauto._

object Reference {
  case class EncryptedReference(reference: String)

  implicit val encoder: Encoder[EncryptedReference] = deriveEncoder[EncryptedReference]
  implicit val decoder: Decoder[EncryptedReference] = deriveDecoder[EncryptedReference]
}
