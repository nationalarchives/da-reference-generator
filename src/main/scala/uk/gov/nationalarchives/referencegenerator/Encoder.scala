package uk.gov.nationalarchives.referencegenerator

import uk.gov.nationalarchives.oci.Alphabet.Alphabet
import uk.gov.nationalarchives.oci.{Alphabet, BaseCoder, IncludedAlphabet}

object Encoder {
  val alphabet: Alphabet = Alphabet.loadAlphabet(Right(IncludedAlphabet.CTDb25)) match {
    case Left(error) => throw new Exception(s"Error loading the CTDb25 encoding alphabet", error)
    case Right(alphabet) => alphabet
  }
  val baseNumber = 25
  val referencePrefix = 'Z'

  def encode(counter: Long): String = {
    val alphabetIndices = BaseCoder.encode(counter, baseNumber)
    referencePrefix +: Alphabet.toString(alphabet, alphabetIndices)
  }
}
