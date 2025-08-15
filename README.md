# DA Reference Generator

Repository containing code for a self-contained service to generate unique "references" for records transferred to The National Archives (TNA).

![](https://raw.githubusercontent.com/nationalarchives/tdr-dev-documentation/master/beta-architecture/diagrams/reference-generator.svg)

The service consists of three main components:
* *Lambda function*: the Lambda generates the "references" based on a incremented counter;
* *DynamoDb Table*: the table contains the counter to generate the unique "references";
* *API Gateway*: Provides access to the service from other AWS services and external clients outside of AWS.

## Lambda

The lambda contains the code for generating references.
This Lambda function is triggered by an api gateway event and context object. It retrieves the current value of the counter from DynamoDB, increments it,
and stores the updated value back in DynamoDB. It then generates a unique “reference” using the counter value. Finally, it returns the generated “references” json to the caller like so:
`[
"M3H5J",
"M3H5K"
]`

A 500 response body will be returned if any issues occur when calling the Lambda for example 
* numberofrefs exceeds the limit
* numberofrefs parameter isn't an int
* any dynamodb Exceptions (key not found, unable to update, etc)

The number of references that can be returned in a single call is limited, because:
* prevent a single call using up all possible references;
* limits to the permitted size of the response.

Calling clients will need to handle this limit by making multiple calls to retrieve the required number of references if it is greater than the limit.

The limit is stored here for use by calling clients: [reference_generator_limit](https://github.com/nationalarchives/da-terraform-configurations/blob/main/tdr/main.tf)

## DynamoDb Table

The DynamoDb stores the current counter used for generating unique references. The DynamoDb is encrypted so that it cannot be directly modified via the AWS console or AWS CLI.
Below is an example of what the table looks like:

| v1          | referenceCounter |
|-------------|------------------|
| fileCounter | 6                |

## API Gateway

There are two REST API Gateways that provides an interface to the Lambda function. 

One is a public and the other private.  Both consume the same Lambda.

Currently, the private API is called from within TNA.  The public API is used by TDR.

The urls for calling the reference generator can be obtained from the [da-terraform-configurations](https://github.com/nationalarchives/da-terraform-configurations/blob/main/tdr/main.tf#L35-L37)
It can be called directly by providing the parameter `numberofrefs={value}` by making a http request to one of the reference generator urls, for example:
`https://j8ezi9m4z0.execute-api.eu-west-2.amazonaws.com/intg/counter?numberofrefs=2`.

The api gateway has a resource policy which restricts which services can call it.

Any new calling clients will need to provide an AWS IAM role or VPCE id which should then be added to the corresponding API Gateway resource policy.

## Reference Schema

The references generated are based on the CTDb52 algorithm which is hosted on [OCI tools scala](https://github.com/nationalarchives/oci-tools-scala/tree/main/src/main/resources/uk/gov/nationalarchives/oci).
Every reference generated will have 'Z' prefixed to it, for example ["ZD4G25","ZD4G26"].

The last permitted reference that should be used is: "WWWWWWW" which corresponds to counter value 6103515624.

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

The Terraform directory contains the Terraform code used to create the DynamoDb, Lambda and API Gateway resources.
It relies on the `da-terraform-configurations` and `da-terraform-modules` projects.

1. Clone DA Terraform Configurations repository

   ```
   [location of project] $ git clone https://github.com/nationalarchives/da-terraform-configurations.git
   ```

2. Clone DA Terraform Modules repository

   ```
   [location of project] $ git clone https://github.com/nationalarchives/da-terraform-modules.git
   ```

#### Apply Terraform 
Commit and push all the changes made in the terraform directory to its GitHub repo, then (in the GitHub repo):

Go the Actions tab -> Click ["Apply Terraform and deploy lambda"] -> Click "Run workflow" -> select the branch with the workflow file you want to use -> type the version to deploy -> Click the green "Run worklfow" button

## Moving to new hosting project

Should the reference generator service need to be moved to a different hosting project then the following steps will need to be taken:

***NOTE*** Before the move the current counter value will need to be noted down to ensure the new DynamoDb table is seeded with the correct counter to prevent duplicate references

* Add relevant Github Actions workflows for the new project to allow testing and deployment 
* Update the [da-terraform-configurations](https://github.com/nationalarchives/da-terraform-configurations) repo with the new values for the reference generator service in the relevant project file:
  * `reference_generator_limit`
  * `reference_generator_intg_url` 
  * `reference_generator_staging_url`
  * `reference_generator_prod_url`
* Ensure the Cloud Custodian rules are implemented for on the new hosting project: [DynamoDb > Security](#security)
* Set up relevant GitHub actions in the new hosting project: [Deployment](#deployment)
