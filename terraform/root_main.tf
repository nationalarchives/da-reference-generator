module "terraform_config_hosting_project" {
  source  = "./da-terraform-configurations/"
  project = var.hosting_project
}

module "dynamodb" {
  source                         = "./da-terraform-modules/dynamo"
  table_name                     = "${var.project}-reference-counter"
  hash_key                       = { type : "S", name : local.dynamodb_hash_key }
  deletion_protection_enabled    = true
  server_side_encryption_enabled = true
  kms_key_arn                    = module.dynamodb_kms_key.kms_key_arn
  point_in_time_recovery_enabled = true
}

module "dynamodb_kms_key" {
  source   = "./da-terraform-modules/kms"
  key_name = "${var.project}-reference-counter-key-${local.hosting_environment}"
  tags     = local.hosting_common_tags
  default_policy_variables = {
    user_roles    = []
    ci_roles      = [local.hosting_assume_role]
    service_names = ["cloudwatch"]
  }
}
