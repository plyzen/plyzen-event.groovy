import groovy.json.JsonOutput

def plyzenIngest(String namespace, String artifact, String version, String environment, String activity, String event, String result) {
    // never break the build in case of error!
    try {
      def payload = [
        "namespace": namespace,
        "artifact": artifact,
        "version": version,
        "environment": environment,
        "activity": activity,
        "event": event,
        "timestamp": new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")),
        "result": result
      ]
      def jsonPayload = JsonOutput.toJson(payload)
      
      // Set Environment variable PLYZEN_ENDPOINT in Jenkins/Pipeline settings
      echo "Ingesting to ${PLYZEN_ENDPOINT}: ${jsonPayload}"

      // HTTP Request Jenkins Plugin needed
      def response = httpRequest acceptType: 'APPLICATION_JSON', contentType: 'APPLICATION_JSON',
                                httpMode: 'POST', quiet: true,
                                requestBody: jsonPayload,
                                url: env.PLYZEN_ENDPOINT,
                                customHeaders: [[name: 'Authorization', value: env.PLYZEN_API_KEY]]

      echo "${PLYZEN_ENDPOINT} resonded with status ${response.status} and content: ${response.content}"
    } catch (Exception e) {
      echo "WARNING: An error occurred in plyzen instrumentation: ${e.message}"
    }
}

def plyzenInstrument(String namespace, String artifact, String environment, Closure step) {
  plyzenIngest(namespace, artifact, env.BUILD_ID, environment, env.STAGE_NAME, "start", "success")
  try {
    step.call()
    plyzenIngest(namespace, artifact, env.BUILD_ID, environment, env.STAGE_NAME, "finish", "success")
  } catch (Exception e) {
    plyzenIngest(namespace, artifact, env.BUILD_ID, environment, env.STAGE_NAME, "finish", "failure")
    throw e
  }
}
