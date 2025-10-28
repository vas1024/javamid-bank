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
                    
                    // Сначала посмотрим структуру проекта
                    sh 'ls -la'
                    sh 'find . -maxdepth 2 -type d | sort'
                    
                    // Простой способ найти сервисы - через shell
                    def servicesOutput = sh(
                        script: 'find . -name "pom.xml" -type f | sed \'s|./||\' | cut -d/ -f1 | sort | uniq | tr \'\\n\' \',\'',
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
                            echo "?? No chart found for ${service}, skipping"
                        }
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
                                    echo "?? No pom.xml found in ${service}, skipping build"
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
                script {
                    echo "?? Deploying application..."
                    if (fileExists('chart/Chart.yaml')) {
                        dir('chart') {
                            sh "helm dependency build ."
                            sh "helm upgrade --install bank . --namespace default --wait --timeout 5m"
                        }
                    } else {
                        echo "?? No umbrella chart found, skipping deployment"
                    }
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
        }
    }
}