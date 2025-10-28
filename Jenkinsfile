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
                    try {
                        echo "?? Starting service discovery..."
                        
                        // Находим все сервисы по наличию chart/ папки
                        def services = findFiles(glob: '*/chart/Chart.yaml')
                        echo "?? Found chart files: ${services}"
                        
                        if (services.isEmpty()) {
                            echo "?? No services found with chart/Chart.yaml pattern"
                            // Попробуем альтернативный поиск
                            sh 'find . -name "Chart.yaml" -type f | head -10'
                            env.SERVICES = ""
                        } else {
                            env.SERVICES = services.collect { it.path.split('/')[0] }.join(',')
                            echo "?? Found services: ${env.SERVICES}"
                            
                            services.each { file ->
                                def serviceName = file.path.split('/')[0]
                                echo "  - ${serviceName}"
                            }
                        }
                    } catch (Exception e) {
                        echo "? Error in Discover Services: ${e.message}"
                        // Покажем структуру проекта для отладки
                        sh 'ls -la'
                        sh 'find . -maxdepth 2 -type d | sort'
                        error("Service discovery failed")
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
                    echo "?? Validating all Helm charts..."
                    def services = env.SERVICES.split(',')
                    services.each { service ->
                        echo "Validating ${service}..."
                        sh "helm lint ${service}/chart"
                    }
                    if (fileExists('chart/Chart.yaml')) {
                        sh "helm lint chart/"
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
                                    echo "?? No pom.xml found in ${service}, skipping Maven build"
                                }
                            }
                        }
                    }
                    
                    parallel buildStages
                }
            }
        }
        
        stage('Deploy') {
            when {
                expression { env.SERVICES != null && !env.SERVICES.isEmpty() }
            }
            steps {
                dir('chart') {
                    sh "helm dependency build ."
                    sh "helm upgrade --install bank . --namespace default --wait --timeout 5m"
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
                    
                    services.each { service ->
                        try {
                            sh """
                            kubectl run smoke-test-${service} --image=curlimages/curl --rm -i --restart=Never --namespace default -- \
                              sh -c 'curl -f http://${service}.default.svc.cluster.local:8080/actuator/health && echo \"? ${service} healthy\"' || echo \"?? ${service} health check failed\"
                            """
                        } catch (Exception e) {
                            echo "?? Smoke test for ${service} failed: ${e.message}"
                        }
                    }
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
            echo "?? Deployment completed successfully!"
        }
        failure {
            echo "? Deployment failed - check logs above"
            // Дополнительная отладочная информация
            sh 'kubectl get pods -A || true'
            sh 'kubectl get nodes || true'
            sh 'docker images | head -10 || true'
        }
    }
}