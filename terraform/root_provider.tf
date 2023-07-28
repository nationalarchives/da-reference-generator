provider "aws" {
  region = "eu-west-2"

  assume_role {
    role_arn     = local.hosting_assume_role
    session_name = "terraform"
    external_id  = module.terraform_config_hosting_project.terraform_config[local.hosting_environment]["terraform_external_id"]
  }
}