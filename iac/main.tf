terraform {
  backend "s3" {
    bucket = "levio-aws-demo-form-backend-terraform"
    key    = "terraform.tfstate"
    region = "us-west-2"
  }
}

provider "aws" {
  region = var.aws_region
}

// LAMBDA FUNCTION
resource "aws_lambda_function" "create_person_function" {
  function_name = var.function_name
  role          = aws_iam_role.create_person_function_role.arn
  handler       = var.handler
  runtime       = "java17"
  memory_size   = 512

  filename         = var.path_to_artifact
  source_code_hash = filebase64sha256(var.path_to_artifact)

  environment {
    variables = var.env_vars
  }

  timeout = 10
}

resource "aws_iam_role" "create_person_function_role" {
  name = "${var.function_name}_role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "create_person_function_role_policy_attachment_lambda_basic_execution_role" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = aws_iam_role.create_person_function_role.name
}

resource "aws_iam_role_policy_attachment" "create_person_function_role_policy_attachment_dynamodb_full_access" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
  role       = aws_iam_role.create_person_function_role.name
}

// API GATEWAY
resource "aws_api_gateway_rest_api" "api_gateway_rest_api" {
  name = "${var.function_name}_api_gateway_rest_api"
}

resource "aws_api_gateway_resource" "api_gateway_resource" {
  rest_api_id = aws_api_gateway_rest_api.api_gateway_rest_api.id
  parent_id   = aws_api_gateway_rest_api.api_gateway_rest_api.root_resource_id
  path_part   = var.path_part
}

module "cors" {
  source = "squidfunk/api-gateway-enable-cors/aws"
  version = "0.3.3"

  api_id          = aws_api_gateway_rest_api.api_gateway_rest_api.id
  api_resource_id = aws_api_gateway_resource.api_gateway_resource.id
}

resource "aws_api_gateway_method" "api_gateway_method" {
  rest_api_id   = aws_api_gateway_rest_api.api_gateway_rest_api.id
  resource_id   = aws_api_gateway_resource.api_gateway_resource.id
  http_method   = var.http_method
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "api_gateway_integration" {
  rest_api_id             = aws_api_gateway_rest_api.api_gateway_rest_api.id
  resource_id             = aws_api_gateway_resource.api_gateway_resource.id
  http_method             = aws_api_gateway_method.api_gateway_method.http_method
  integration_http_method = var.http_method
  type                    = "AWS_PROXY"
  uri                     = "arn:aws:apigateway:${var.aws_region}:lambda:path/2015-03-31/functions/${aws_lambda_function.create_person_function.arn}/invocations"
}

resource "aws_api_gateway_deployment" "api_gateway_deployment" {
  depends_on = [aws_api_gateway_integration.api_gateway_integration]

  rest_api_id = aws_api_gateway_rest_api.api_gateway_rest_api.id
  stage_name  = var.stage_name
}

resource "aws_lambda_permission" "lambda_permission" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = var.function_name
  principal     = "apigateway.amazonaws.com"

  source_arn = "arn:aws:execute-api:${var.aws_region}:${data.aws_caller_identity.current.account_id}:${aws_api_gateway_rest_api.api_gateway_rest_api.id}/*/${aws_api_gateway_method.api_gateway_method.http_method}${aws_api_gateway_resource.api_gateway_resource.path}"
}

data "aws_caller_identity" "current" {}