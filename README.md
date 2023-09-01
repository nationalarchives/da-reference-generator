# DA Reference Generator

Repository containing code for a self-contained service to generate unique "references" for records transferred to The National Archives (TNA).

The service consists of three main components:
* *Lambda function*: the Lambda generates the "references" based on a incremented counter;
* *DynamoDb Table*: the table contains the counter to generate the unique "references";
* *API Gateway*: Provides access to the service from other AWS services and external clients outside of AWS.

## Lambda

[TODO]

## DynamoDb Table

[TODO]

## API Gateway

[TODO]

## Deployment

Deployment process of the service will depend on the hosting environment.

GitHub Actions workflow files should be prefixed with the hosting environment, eg `tdr_`

### Current Hosting Environment: TDR

The service is currently hosted on [Transfer Digital Records (TDR)](https://github.com/nationalarchives/tdr-dev-documentation)

#### Repository and environment secrets

These are set in the [tdr-terraform-github](https://github.com/nationalarchives/tdr-terraform-github) repository

* `MANAGEMENT_ACCOUNT`: AWS account number for the TDR management (mgmt) account.
* `WORKFLOW_PAT`: GitHub access token for TDR
* `SLACK_WEBHOOK`: TDR slack webhook

#### Seeding the Counter

The reference counter in the DynamoDb table needs to be manually seeded before the table is encrypted.

1. Create the DyanmoDb table using the Terraform but not adding the encryption
2. Manually seed the counter in the table either via the AWS Cli or AWS Console:
    ```
   {
     "v1": {
         "S": "fileCounter"
     },
     "referenceCounter": {
         "N": "0"
     }
   }
   ```
3. Add table encryption using the Terraform

### Terraform

[TODO]

### Lambda Code

[TODO]
