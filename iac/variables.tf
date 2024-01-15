variable "path_to_artifact" {
  description = "Path to the jar file."
  type        = string
}

variable "function_name" {
  description = "The name of the Lambda function."
  type        = string
}

variable "handler" {
  description = "The handler for the Lambda function."
  type        = string
}

variable "env_vars" {
  description = "Map of environment variables"
  type        = map(string)
  default     = {}
}

variable "path_part" {
  description = "The path of Api Gateway"
  type        = string
}

variable "http_method" {
  description = "The http method of Api Gateway"
  type        = string
}

variable "stage_name" {
  description = "The stage name of Api Gateway"
  type        = string
}

variable "aws_region" {
  description = "The AWS region where resources will be created"
  type        = string
  default     = "us-west-2"
}