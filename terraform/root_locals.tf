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
    }
  )
  dynamodb_hash_key                    = "v1"
  reference_generator_function_name    = "${var.project}-reference-generator-${local.hosting_environment}"
  reference_generator_api_gateway_name = "${upper(var.project)}ReferenceGenerator${local.hosting_environment}"
  reference_counter_table_name         = "${var.project}-reference-counter"
  reference_generator_retries          = 2
  reference_generator_limit            = module.terraform_config_hosting_project.terraform_config["reference_generator_limit"]
  tdr_vpc_public_ip                    = module.terraform_config_hosting_project.terraform_config["${local.hosting_environment}_ip_public"]
  wiz_role_arns                        = module.terraform_config_hosting_project.terraform_config[local.hosting_environment]["wiz_role_arns"]
}
