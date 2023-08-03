module "terraform_config_hosting_project" {
  source  = "./da-terraform-configurations/"
  project = var.hosting_project
}

module "dynamodb" {
  source = "./da-terraform-modules/dynamo"
  table_name = "${var.project}-reference-counter"
  hash_key = local.dynamodb_hash_key
  hash_key_type = "S"
  deletion_protection_enabled = true
}

resource "aws_iam_policy" "deny_counter_delete_access_policy" {
  name = "${title(var.project)}ReferenceCounterTableAccessPolicyIntg"
  policy = templatefile("${path.module}/templates/explicit_deny_alter_counter.json.tpl", { table_arn = module.dynamodb.table_arn, leading_key = local.dynamodb_leading_key })
}
