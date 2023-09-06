package uk.gov.nationalarchives.referencegenerator

import uk.gov.nationalarchives.referencegenerator.Lambda.Input

object LambdaRunner extends App {
  new Lambda().process(Input(0))
}
