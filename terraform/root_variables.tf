variable "project" {
  description = "abbreviation for the project, e.g. da, forms the first part of resource names"
  default = "da"
}

variable "account_number" {
  description = "The AWS account number where the service is hosted is hosted"
  type        = string
}
