terraform {
  backend "s3" {
    bucket         = "tdr-terraform-state"
    key            = "reference-generator.state"
    region         = "eu-west-2"
    encrypt        = true
    dynamodb_table = "tdr-terraform-state-lock"
  }
}
