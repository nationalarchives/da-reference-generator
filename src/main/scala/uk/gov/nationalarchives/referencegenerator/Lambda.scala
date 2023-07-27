package uk.gov.nationalarchives.referencegenerator

import uk.gov.nationalarchives.referencegenerator.Lambda.Input

class Lambda {

  def process(input: Input): Unit = { }
}

object Lambda {
  case class Input(numberOfReferences: Int)
}
