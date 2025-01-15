{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": "*",
      "Action": "execute-api:Invoke",
      "Resource": "${api_gateway_arn}/*"
    },
    {
      "Effect": "Deny",
      "Principal": "*",
      "Action": "execute-api:Invoke",
      "Resource": "${api_gateway_arn}/*",
      "Condition": {
        "NotIpAddress": {
          "aws:SourceIp": ${tdr_vpc_public_ip}
        }
      }
    }
  ]
}
