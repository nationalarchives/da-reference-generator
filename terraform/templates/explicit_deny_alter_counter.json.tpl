{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "ExplicitDenyDeleteCounter",
      "Effect": "Deny",
      "Action": [
        "dynamodb:DeleteItem",
        "dynamodb:UpdateItem"
      ],
      "Resource": "${table_arn}",
      "Condition": {
        "ForAllValues:StringEquals": {
          "dynamodb:LeadingKeys": "${leading_key}"
        }
      }
    }
  ]
}
