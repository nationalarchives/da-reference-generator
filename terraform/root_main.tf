module "terraform_config_hosting_project" {
  source  = "./da-terraform-configurations/"
  project = var.hosting_project
}

module "dynamodb" {
  source                         = "./da-terraform-modules/dynamo"
  table_name                     = local.reference_counter_table_name
  hash_key                       = { type : "S", name : local.dynamodb_hash_key }
  deletion_protection_enabled    = true
  server_side_encryption_enabled = true
  kms_key_arn                    = module.dynamodb_kms_key.kms_key_arn
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

module "reference_generator_lambda" {
  source        = "./da-terraform-modules/lambda"
  function_name = local.reference_generator_function_name
  handler       = "uk.gov.nationalarchives.referencegenerator.Lambda::handleRequest"
  policies = {
    "ReferenceGeneratorLambdaPolicy${title(local.hosting_environment)}" = templatefile("${path.module}/templates/lambda/reference_generator_policy.json.tpl", {
      function_name = local.reference_generator_function_name
      account_id    = data.aws_caller_identity.current.account_id
      table_name    = local.reference_counter_table_name
    })
  }
  runtime = "java11"
  tags    = local.hosting_common_tags
}
