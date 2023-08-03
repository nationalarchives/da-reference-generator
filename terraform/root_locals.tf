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
  dynamodb_leading_key = "filePieceCounter"
  dynamodb_hash_key    = "v1"
}
