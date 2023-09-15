{
  "Version":"2012-10-17",
  "Statement":[
    {
      "Effect":"Allow",
      "Principal":{
        "AWS":[
          "${api_task_role_arn}",
          "${api_execution_role_arn}"
        ]
      },
      "Action":"execute-api:Invoke",
      "Resource":"${api_gateway_arn}/*"
    }
  ]
}
