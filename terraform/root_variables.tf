variable "project" {
  description = "abbreviation for the project, e.g. da, forms the first part of resource names"
  default     = "da"
}

variable "hosting_project" {
  description = "abbreviation for the project hosting the service, eg tdr"
  default     = "tdr"
}

variable "hosting_account_number" {
  description = "The AWS account number where the service is hosted"
  type        = string
}
