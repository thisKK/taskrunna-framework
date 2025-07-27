#!/bin/bash

echo "🚀 Starting TaskRunna Prometheus Metrics Example..."

# Start the example in background
./gradlew :taskrunna-examples:run --quiet &
EXAMPLE_PID=$!

echo "⏳ Waiting for server to start..."
sleep 10

echo "📊 Testing metrics endpoint..."
curl -s http://localhost:8080/metrics | grep order_retry | head -10

echo ""
echo "🏠 Testing home page..."
curl -s http://localhost:8080/ | head -5

echo ""
echo "🔍 Server is running at http://localhost:8080"
echo "📊 Metrics available at http://localhost:8080/metrics"
echo ""
echo "🛑 Stopping server..."
kill $EXAMPLE_PID
sleep 2

echo "✅ Test completed!" 