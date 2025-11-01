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
                    def chartYaml = readFile file: 'chart/Chart.yaml'
                    def services = parseDependenciesFromYaml(chartYaml)
                    env.SERVICES = services.join(',')
                    echo "Services from Chart.yaml: ${env.SERVICES}"
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
    steps {
        script {
            echo "?? Running Smoke Tests..."
            
            // Удаляем старый тестовый под
            sh 'kubectl delete pod smoke-test-bank --ignore-not-found=true --namespace default'
            
            // Формируем команду для всех сервисов
            def services = env.SERVICES.split(',')
            def curlCommands = services.collect { service ->
                "curl -f http://${service}:8080/actuator/health && echo \"? ${service} healthy\""
            }.join(' && ')
            
            // Создаем под с одной командой
            sh """
            kubectl run smoke-test-bank \
                --image=curlimages/curl \
                --namespace default \
                --restart=Never \
                -- \
                sh -c '
                  echo "?? Starting comprehensive smoke tests..." &&
                  ${curlCommands} &&
                  echo "?? All services are healthy!"
                '
            """
            
            // Ждем и логируем
            sh '''
            echo "? Waiting for tests to complete..."
            kubectl wait --for=condition=Ready pod/smoke-test-all --timeout=60s --namespace default
            kubectl logs smoke-test-all --namespace default -f
            '''
            
            // Проверяем результат
            def result = sh(
                script: 'kubectl get pod smoke-test-all -o jsonpath="{.status.phase}" --namespace default',
                returnStdout: true
            ).trim()

           // УДАЛЯЕМ ПОД ПОСЛЕ ТЕСТОВ
            sh 'kubectl delete pod smoke-test-bank --namespace default --ignore-not-found=true'
            
            if (result != "Succeeded") {
                error "? Smoke tests failed"
            } else {
                echo "? All smoke tests passed!"
            }
        }
    }
}



stage('Helm Tests') {
    steps {
        script {
            echo "?? Running Helm tests..."
            sh "helm test bank --namespace default --timeout 3m"
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







def parseDependenciesFromYaml(String yamlContent) {
    def services = []
    def lines = yamlContent.split('\n')
    def inDependencies = false
    def currentService = null
    
    echo "?? Starting YAML parsing..."
    echo "Total lines: ${lines.size()}"
    
    lines.eachWithIndex { line, index ->
        def trimmed = line.trim()
        echo "Line ${index}: '${trimmed}'"
        
        if (trimmed == 'dependencies:') {
            inDependencies = true
            echo "? Entered dependencies section"
        } else if (inDependencies && !trimmed.startsWith('-') && !trimmed.startsWith(' ') && trimmed.contains(':')) {
            // Вышли из dependencies (нашли другой корневой ключ)
            inDependencies = false
            echo "? Exited dependencies section (found other root key: ${trimmed})"
        } else if (inDependencies && trimmed.startsWith('- name:')) {
            // Нашли сервис
            def serviceName = trimmed.replace('- name:', '').trim()
            serviceName = serviceName.replaceAll('"', '').replaceAll("'", "")
            services.add(serviceName)
            echo "?? Found service: ${serviceName}"
        } else if (inDependencies && trimmed.startsWith('name:')) {
            // Альтернативный формат (без дефиса)
            def serviceName = trimmed.replace('name:', '').trim()
            serviceName = serviceName.replaceAll('"', '').replaceAll("'", "")
            services.add(serviceName)
            echo "?? Found service (alt format): ${serviceName}"
        }
    }
    
    echo "?? Final services list: ${services}"
    return services
}



















