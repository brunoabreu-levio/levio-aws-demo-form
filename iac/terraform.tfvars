path_to_artifact = "../CreatePersonFunction/target/create-person-1.0.jar"
function_name    = "create_person_function"
handler          = "com.levio.awsdemo.createperson.App::handleRequest"
env_vars         = {
  CORS_ORIGIN       = "*",
  JAVA_TOOL_OPTIONS = "-XX:+TieredCompilation -XX:TieredStopAtLevel=1",
}
path_part   = "person"
http_method = "POST"
stage_name  = "PROD"