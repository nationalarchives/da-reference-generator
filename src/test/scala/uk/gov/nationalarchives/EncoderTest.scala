package uk.gov.nationalarchives

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.nationalarchives.oci.{Alphabet, IncludedAlphabet}
import uk.gov.nationalarchives.referencegenerator._

class EncoderTest extends AnyFlatSpec with Matchers {

  "Encoder" should "encode a counter correctly" in {
    val counter = 123L
    val expectedEncodedString = "Z6V"

    val actualEncodedString = Encoder.encode(counter)

    actualEncodedString shouldEqual expectedEncodedString
  }

  "Encoder" should "throw an exception if the alphabet loading fails" in {
    val exception = Alphabet.loadAlphabet(Left("/fake/path"))
    exception.left.map(_.getMessage) shouldEqual Left("No such file: /fake/path")
  }

  "Encoder" should "have baseNumber equal to 25" in {
    Encoder.baseNumber shouldEqual 25
  }

  "Encoder" should "have referencePrefix equal to 'Z'" in {
    Encoder.referencePrefix shouldEqual 'Z'
  }
}
