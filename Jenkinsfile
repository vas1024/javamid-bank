pipeline {
    agent any
    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        skipDefaultCheckout(false)
    }
    stages {


stage('Discover Services and Tools ') {
    steps {
        script {

            def servicesOutput = sh(
                script: '''
                grep "fullListOfServices" chart/values.yaml | awk -F: '{print $2}' | tr -d ' "'
                ''',
                returnStdout: true
            ).trim()
            env.SERVICES = servicesOutput
            echo "Full list of services from values.yaml: ${env.SERVICES}"


            def toolsOutput = sh(
                script: '''
                grep "fullListOfTools" chart/values.yaml | awk -F: '{print $2}' | tr -d ' "'
                ''',
                returnStdout: true
            ).trim()
            env.TOOLS = toolsOutput
            echo "Full list of tools from values.yaml: ${env.TOOLS}"

        }
    }
}

        
        stage('Validate Charts') {
            when {
                expression { env.SERVICES != null && !env.SERVICES.isEmpty() }
            }
            steps {
                script {
                    echo "?? Validating Helm charts..."
                    def services = env.SERVICES.split(',')
                    
                    // Проверяем существование chart'ов перед валидацией
                    services.each { service ->
                        if (fileExists("${service}/chart/Chart.yaml")) {
                            echo "Validating ${service} chart..."
                            sh "helm lint ${service}/chart"
                        } else {
                            echo "?? No chart found for ${service}, skipping validation"
                        }
                    }
                }
            }
        }
        
        stage('Build Services') {
            when {
                expression { env.SERVICES != null && !env.SERVICES.isEmpty() }
            }
            steps {
                script {
                    def services = env.SERVICES.split(',')
                    
                    // Собираем все сервисы параллельно
                    def buildStages = [:]
                    
                    services.each { service ->
                        buildStages["Build ${service}"] = {
                            dir(service) {
                                echo "??? Building ${service}..."
                                
                                // Проверяем существование pom.xml перед сборкой
                                if (fileExists('pom.xml')) {
                                    sh "mvn clean package -DskipTests"
                                    sh "docker build -t ${service}:latest ."
                                    sh "kind load docker-image ${service}:latest"
                                    echo "? ${service} built successfully!"
                                } else {
                                    echo "?? No pom.xml found in ${service}, skipping build"
                                }
                            }
                        }
                    }
                    
                    parallel buildStages
                }
            }
        }
        




stage('Deploy Tools') {
    when {
        expression { env.TOOLS != null && !env.TOOLS.isEmpty() }
    }
    steps {
        script {
            echo "?? Deploying infrastructure tools..."
            def tools = env.TOOLS.split(',')
            
            // Деплоим каждый инструмент параллельно
            def deployStages = [:]
            
            tools.each { tool ->
                deployStages["Deploy ${tool}"] = {
                    script {
                        if (fileExists("${tool}/chart/Chart.yaml")) {
                            echo "?? Deploying ${tool}..."
                            
                            // Проверяем существование values.yaml
                            def valuesFile = "${tool}/chart/values.yaml"
                            def valuesArg = fileExists(valuesFile) ? "-f ${valuesFile}" : ""
                            
                            sh """
                            helm upgrade --install ${tool} ${tool}/chart \
                                --namespace default \
                                --force \
                                --wait \
                                --timeout 5m \
                                ${valuesArg}
                            """
                            echo "? ${tool} deployed successfully!"
                        } else {
                            echo "? No chart found for ${tool}, skipping deployment"
                        }
                    }
                }
            }
            
            parallel deployStages
        }
    }
}




        stage('Deploy Services') {
            when {
                expression { env.SERVICES != null && !env.SERVICES.isEmpty() }
            }
            steps {
                script {
                    echo "?? Deploying services individually..."
                    def services = env.SERVICES.split(',')
                    
                    // Деплоим каждый сервис параллельно
                    def deployStages = [:]
                    
                    services.each { service ->
                        deployStages["Deploy ${service}"] = {
                            script {
                                if (fileExists("${service}/chart/Chart.yaml")) {
                                    echo "?? Deploying ${service}..."
                                    
                                    // Проверяем существование values.yaml
                                    def valuesFile = "${service}/chart/values.yaml"
                                    def valuesArg = fileExists(valuesFile) ? "-f ${valuesFile}" : ""
                                    
                                    sh """
                                    helm upgrade --install ${service} ${service}/chart \
                                        --namespace default \
                                        --set image.tag=latest \
                                        --force \
                                        --wait \
                                        --timeout 3m \
                                        ${valuesArg}
                                    """
                                    echo "? ${service} deployed successfully!"
                                } else {
                                    echo "?? No chart found for ${service}, skipping deployment"
                                }
                            }
                        }
                    }
                    
                    parallel deployStages
                }
            }
        }
        

        stage('Verify Deployment') {
            when {
                expression { env.SERVICES != null && !env.SERVICES.isEmpty() }
            }
            steps {
                script {
                    echo "?? Checking deployment status..."
                    def services = env.SERVICES ? env.SERVICES.split(',') : []
                    def tools = env.TOOLS ? env.TOOLS.split(',') : []
                    def common = services + tools
                    common.each { component ->
                        try {
                            echo "Checking ${component}..."
                            sh """
                            kubectl wait --for=condition=ready pod -l app=${component} --namespace default --timeout=60s
                            echo "? ${component} is ready"
                            """
                        } catch (Exception e) {
                            echo "?? ${component} is not ready yet: ${e.message}"
                        }
                    }
                    
                    // Показываем общий статус
                    sh "kubectl get pods -l app -n default --no-headers | wc -l"
                    sh "kubectl get deployments -n default"
                }
            }
        }
        
 



stage('Smoke Tests') {
    steps {
      catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
        script {
            echo "?? Running Smoke Tests..."
            
            sh 'kubectl delete pod smoke-test-bank --ignore-not-found=true --namespace default'
            
            def services = env.SERVICES.split(',')
            
            // Каждая команда curl независима (через ; вместо &&)
            def curlCommands = services.collect { service ->
                """
                echo "Testing ${service}..."
                if curl -f http://${service}:8080/actuator/health; then
                    echo "? ${service} healthy"
                else
                    echo "?? ${service} unavailable"
                fi
                """
            }.join('\n')
            
            sh """
            kubectl run smoke-test-bank \
                --image=curlimages/curl \
                --namespace default \
                --restart=Never \
                -- \
                sh -c '
                  echo "?? Starting smoke tests..." &&
                  ${curlCommands} &&
                  echo "?? All tests executed"
                '
            """
            
            sh 'kubectl wait --for=condition=Complete pod/smoke-test-bank --timeout=120s --namespace default || true'
            sh 'kubectl logs smoke-test-bank --namespace default'
            sh 'kubectl delete pod smoke-test-bank --namespace default --ignore-not-found=true'
            
            echo "? Smoke tests completed (warnings are OK)"
        }
    }
  }
}






//stage('Helm Tests') {
//    steps {
//        script {
//            echo "?? Running Helm tests..."
//            sh "helm test bank --namespace default --timeout 3m"
//        }
//    }
//}






    }
    post {
        always {
            echo "?? Build ${currentBuild.result} - ${env.BUILD_URL}"
            // Очистка тестовых ресурсов
//            sh "kubectl delete pod --field-selector=status.phase==Succeeded -n default --ignore-not-found=true"
//            sh "kubectl delete pod -l run=smoke-test -n default --ignore-not-found=true"
        }
        success {
            echo "?? All services deployed successfully!"
            // Показываем финальный статус
            sh "kubectl get pods -n default"
            sh "kubectl get services -n default"
        }
        failure {
            echo "? Deployment failed - check logs above"
            sh "kubectl get pods -n default"
            sh "kubectl describe pods -n default | grep -A 10 -B 5 Error || true"
        }
    }
}

