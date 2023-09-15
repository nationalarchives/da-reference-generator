{
  "swagger" : "2.0",
  "info" : {
    "description" : "API Gateway for Reference Generator",
    "version" : "2023-09-11T11:10:51Z",
    "title" : "${title}"
  },
  "basePath" : "/${environment}",
  "schemes" : [ "https" ],
  "paths" : {
    "/counter" : {
      "get" : {
        "produces" : [ "application/json" ],
        "parameters" : [ {
          "name" : "numberofrefs",
          "in" : "query",
          "required" : true,
          "type" : "string"
        } ],
        "responses" : {
          "200" : {
            "description" : "200 response",
            "schema" : {
              "$ref" : "#/definitions/Empty"
            }
          }
        },
        "x-amazon-apigateway-request-validator" : "Validate query string parameters and headers",
        "x-amazon-apigateway-integration" : {
          "type" : "aws_proxy",
          "httpMethod" : "POST",
          "uri" : "arn:aws:apigateway:eu-west-2:lambda:path/2015-03-31/functions/${lambda_arn}/invocations",
          "responses" : {
            "default" : {
              "statusCode" : "200"
            }
          },
          "passthroughBehavior" : "when_no_match",
          "contentHandling" : "CONVERT_TO_TEXT"
        }
      }
    }
  },
  "definitions" : {
    "Empty" : {
      "type" : "object",
      "title" : "Empty Schema"
    }
  },
  "x-amazon-apigateway-request-validators" : {
    "Validate query string parameters and headers" : {
      "validateRequestParameters" : true,
      "validateRequestBody" : false
    }
  }
}
