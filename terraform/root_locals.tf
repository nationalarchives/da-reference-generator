locals {
  environment = lower(terraform.workspace)
  common_tags = tomap(
    {
      "Environment" = local.environment,
      "Owner"       = "digital archiving",
      "Terraform"   = true,
      "TerraformSource" = "https://github.com/nationalarchives/da-reference-generator",
      "CostCentre"  = "56"
      "Role" = "prvt"
    }
  )
}
