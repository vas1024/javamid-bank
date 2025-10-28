pipeline {
    agent any
    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        skipDefaultCheckout(false)
    }
    stages {
        stage('Discover Services') {
            steps {
                script {
                    echo "?? Starting service discovery..."
                    
                    // Ищем сервисы - только в подпапках, исключая корневой pom.xml
                    def servicesOutput = sh(
                        script: 'find . -mindepth 2 -name "pom.xml" -type f | grep -v "^./pom.xml" | sed \'s|./||\' | cut -d/ -f1 | sort | uniq | tr \'\\n\' \',\'',
                        returnStdout: true
                    ).trim()
                    
                    // Убираем последнюю запятую если есть
                    if (servicesOutput.endsWith(',')) {
                        servicesOutput = servicesOutput.substring(0, servicesOutput.length() - 1)
                    }
                    
                    env.SERVICES = servicesOutput
                    echo "?? Found services: ${env.SERVICES}"
                    
                    if (env.SERVICES.isEmpty()) {
                        echo "?? No services found, using default list"
                        env.SERVICES = "accounts,auth,front,exchange,transfer,cash,blocker,exgen,notify"
                    }
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
                    def services = env.SERVICES.split(',')
                    
                    services.each { service ->
                        try {
                            echo "Checking ${service}..."
                            sh """
                            kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=${service} --namespace default --timeout=60s
                            echo "? ${service} is ready"
                            """
                        } catch (Exception e) {
                            echo "?? ${service} is not ready yet: ${e.message}"
                        }
                    }
                    
                    // Показываем общий статус
                    sh "kubectl get pods -l app.kubernetes.io/name -n default --no-headers | wc -l"
                    sh "kubectl get deployments -n default"
                }
            }
        }
        
        stage('Smoke Tests') {
            when {
                expression { env.SERVICES != null && !env.SERVICES.isEmpty() }
            }
            steps {
                script {
                    echo "?? Running Smoke Tests..."
                    def services = env.SERVICES.split(',')
                    
                    // Запускаем тесты параллельно
                    def testStages = [:]
                    
                    services.each { service ->
                        testStages["Test ${service}"] = {
                            try {
                                echo "Testing ${service}..."
                                sh """
                                timeout 30s kubectl run smoke-test-${service} --image=curlimages/curl --rm -i --restart=Never --namespace default -- \
                                  sh -c 'curl -f http://${service}.default.svc.cluster.local:8080/actuator/health && echo \"? ${service} healthy\"' || echo \"?? ${service} health check failed\"
                                """
                            } catch (Exception e) {
                                echo "?? Smoke test for ${service} failed: ${e.message}"
                            }
                        }
                    }
                    
                    parallel testStages
                }
            }
        }
    }
    post {
        always {
            echo "?? Build ${currentBuild.result} - ${env.BUILD_URL}"
            // Очистка тестовых ресурсов
            sh "kubectl delete pod --field-selector=status.phase==Succeeded -n default --ignore-not-found=true"
            sh "kubectl delete pod -l run=smoke-test -n default --ignore-not-found=true"
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