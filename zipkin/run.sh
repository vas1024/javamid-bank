helm repo add openzipkin https://openzipkin.github.io/zipkin
helm repo update openzipkin
helm upgrade --install zipkin openzipkin/zipkin  --namespace $NAMESPACE --wait --timeout 5m