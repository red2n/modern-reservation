#!/bin/bash

# Modern Reservation MCP Server - Quick Test Script

echo "==================================="
echo "MCP Server Quick Test"
echo "==================================="
echo ""

# Check if server is built
if [ ! -f "dist/index.js" ]; then
    echo "❌ Server not built. Building now..."
    npm run build
    if [ $? -ne 0 ]; then
        echo "❌ Build failed!"
        exit 1
    fi
    echo "✅ Build successful"
else
    echo "✅ Server is built"
fi

echo ""
echo "Testing server startup..."
echo ""

# Test server startup (2 second timeout)
OUTPUT=$(timeout 2s node dist/index.js 2>&1 | head -n 1)
EXIT_CODE=$?

echo "$OUTPUT"

if [ $EXIT_CODE -eq 124 ] || [ $EXIT_CODE -eq 0 ]; then
    echo ""
    echo "✅ Server started successfully!"
    echo ""
    echo "==================================="
    echo "MCP Server is Ready!"
    echo "==================================="
    echo ""
    echo "You can now use it with Copilot:"
    echo ""
    echo "1. Ask Copilot: 'Should I create a README here?'"
    echo "2. Ask Copilot: 'Show me schema best practices'"
    echo "3. Ask Copilot: 'Validate my approach to...'"
    echo ""
    echo "Configuration:"
    echo "  - VS Code: .vscode/settings.json"
    echo "  - Instructions: .github/copilot-instructions.md"
    echo ""
else
    echo ""
    echo "❌ Server failed to start"
    echo "Run: npm run build"
    exit 1
fi
