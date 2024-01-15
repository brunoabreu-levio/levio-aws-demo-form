output "lambda_arn" {
  value = aws_lambda_function.create_person_function.arn
}

output "api_gateway_url" {
  value = aws_api_gateway_deployment.api_gateway_deployment.invoke_url
}