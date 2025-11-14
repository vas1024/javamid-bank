docker pull logstash:7.17.3
kind load docker-image logstash:7.17.3
docker pull elasticsearch:9.1.7
kind load docker-image elasticsearch:9.1.7
docker pull kibana:9.1.7     
kind load docker-image kibana:9.1.7
git clone https://github.com/elastic/helm-charts.git
helm install logstash ./helm-charts/logstash -f ./logstash-values.yaml