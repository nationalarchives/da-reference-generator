package uk.gov.nationalarchives.referencegenerator

import uk.gov.nationalarchives.oci.Alphabet.Alphabet
import uk.gov.nationalarchives.oci.{Alphabet, BaseCoder, IncludedAlphabet}

object Base25Encoder {
  val alphabet: Alphabet = Alphabet.loadAlphabet(Right(IncludedAlphabet.GCRb25)) match {
    case Left(error) => throw new Exception(s"Error loading the GCRb25 encoding alphabet", error)
    case Right(alphabet) => alphabet
  }
  val baseNumber = 25

  def encode(counter: Long): String = {
    val alphabetIndices = BaseCoder.encode(counter, baseNumber)
    Alphabet.toString(alphabet, alphabetIndices)
  }
}
