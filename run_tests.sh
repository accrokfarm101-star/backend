#!/bin/bash
echo "=========================================="
echo "FreshMart Review Management - Test Suite"
echo "=========================================="
echo ""

echo "Step 1: Clean build..."
mvn clean -q

echo "Step 2: Compiling code..."
mvn compile -q

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed!"
    exit 1
fi

echo "✅ Compilation successful"
echo ""

echo "Step 3: Running Review Controller Tests..."
mvn test -Dtest=ReviewControllerTests -q

if [ $? -eq 0 ]; then
    echo "✅ All Review tests passed!"
else
    echo "❌ Some tests failed"
    exit 1
fi

echo ""
echo "Step 4: Running all tests..."
mvn test -q

if [ $? -eq 0 ]; then
    echo "✅ All tests passed successfully!"
else
    echo "⚠️ Some other tests may have failed"
fi

echo ""
echo "=========================================="
echo "Test suite completed!"
echo "=========================================="
