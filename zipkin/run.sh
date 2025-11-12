helm repo add openzipkin https://openzipkin.github.io/zipkin
helm repo update openzipkin
helm install zipkin openzipkin/zipkin