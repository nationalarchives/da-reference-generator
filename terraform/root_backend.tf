terraform {
  backend "s3" {
    key     = "reference-generator.state"
    region  = "eu-west-2"
    encrypt = true
  }
}
