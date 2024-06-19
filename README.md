# plyzen-event.groovy

## About

This repo contains tools for instrumenting your CI/CD pipeline (mainly Jenkins) to send data to [plyzen](https://plyzen.io).

[plyzen](https://plyzen.io) is a cloud-based process data analytics tool for software delivery stakeholders.
plyzen helps to automatically capture and analyze CI/CD process data and derive the for key [DORA Metrics](https://dora.dev/guides/dora-metrics-four-keys/) to measure software delivery performance.

## Instrumenting the CI/CD Pipelines

In order to collect the metrics, events from the CI/CD pipelines (pipeline events) must be transmitted to plyzen. For this purpose, the pipelines are instrumented at appropriate points.

Each pipeline event (supported types: build, deployment, test) must be associated with a version of a software artifact in order to be evaluated.

For example, a simple pipeline event might look like this
```yaml
{
   "namespace": "narwhal",
   "artifact": "foo-api",
   "version": "2.1",
   "environment": "ci",
   "instance": "1",
   "activity": "build",
   "event": "finish",
   "timestamp": "2023-04-06T16:35:26.492Z",
   "result": "success"
}
```

Pipeline events are submitted to the plyzen ingest endpoint (https://in.plyzen.io).

To facilitate instrumentation, this repo provides a Groovy script `plyzen-event.groovy` that can be utilized - e.g. as shared library – to instrument a Jenkins pipeline like this:

```groovy
// Simulate randomly failing stages
def runMockSteps(double successProbability = 0.9) {
    def random = Math.random()
    if (random > successProbability) {
        error("Stage '${env.STAGE_NAME}' failed due to random probability")
    }
    sleep(random * 2)
}

pipeline {
    agent any

    environment {
        // configure api key as secret in Jenkins' credentials store
        PLYZEN_API_KEY = credentials('plyzen-demo-api-key')
    }

    stages {
        stage('build') {
            steps {
                plyzenInstrument("demo", "core-lib", "jenkins") {
                    runMockSteps()
                }
            }
        }
        stage('unit-test') {
            steps {
                plyzenInstrument("demo", "core-lib", "jenkins") {
                    runMockSteps()
                }
            }
        }
        stage('qa-deploy') {
            steps {
                plyzenInstrument("demo", "core-lib", "qa") {
                    runMockSteps()
                }
            }
        }
        stage('qa-test') {
            steps {
                plyzenInstrument("demo", "core-lib", "qa") {
                    runMockSteps()
                }
            }
        }
        stage('prod-deploy') {
            steps {
                plyzenInstrument("demo", "core-lib", "prod") {
                    runMockSteps()
                }
            }
        }
        stage('prod-smoketest') {
            steps {
                plyzenInstrument("demo", "core-lib", "prod") {
                    runMockSteps()
                }
            }
        }
    }
}
```
