locals {
  hosting_environment = lower(terraform.workspace)
  hosting_assume_role = module.terraform_config_hosting_project.terraform_config[local.hosting_environment]["terraform_account_role"]
  hosting_common_tags = tomap(
    {
      "Environment"     = local.hosting_environment,
      "Owner"           = "digital archiving",
      "Terraform"       = true,
      "TerraformSource" = "https://github.com/nationalarchives/da-reference-generator",
      "CostCentre"      = module.terraform_config_hosting_project.terraform_config["cost_centre"]
      "Role"            = "prvt"
  })

  aws_backup_tag              = local.hosting_environment == "prod" ? module.terraform_config_hosting_project.terraform_config["aws_backup_daily_short_term_retain_tag"] : null
  aws_backup_service_role_arn = module.aws_backup_terraform_config.terraform_config["aws_service_backup_role"]
  aws_backup_local_role_name  = module.aws_backup_terraform_config.terraform_config["local_account_backup_role_name"]
  aws_backup_local_role_arn   = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${local.aws_backup_local_role_name}"
  aws_backup_roles            = local.hosting_environment == "prod" ? [local.aws_backup_local_role_arn, local.aws_backup_service_role_arn] : []

  dynamodb_hash_key                    = "v1"
  reference_generator_function_name    = "${var.project}-reference-generator-${local.hosting_environment}"
  reference_generator_api_gateway_name = "${upper(var.project)}ReferenceGenerator${local.hosting_environment}"
  reference_counter_table_name         = "${var.project}-reference-counter"
  reference_generator_retries          = 2
  reference_generator_limit            = module.terraform_config_hosting_project.terraform_config["reference_generator_limit"]
  tdr_vpc_public_ip                    = module.terraform_config_hosting_project.terraform_config["${local.hosting_environment}_ip_public"]
  wiz_role_arns                        = module.terraform_config_hosting_project.terraform_config[local.hosting_environment]["wiz_role_arns"]
}
