#!/bin/bash

# MCP Server Validation Test
# This script tests if the MCP server is working correctly

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║         MCP Server Validation Test                       ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

cd "$(dirname "$0")"

# Test 1: Check if server is built
echo "Test 1: Checking if server is built..."
if [ -f "dist/index.js" ]; then
    echo "  ✅ Server is built"
else
    echo "  ❌ Server not built - running build..."
    npm run build
fi
echo ""

# Test 2: Check if server starts
echo "Test 2: Checking if server starts..."
OUTPUT=$(timeout 1s node dist/index.js 2>&1)
if echo "$OUTPUT" | grep -q "Modern Reservation MCP Server running"; then
    echo "  ✅ Server starts successfully"
else
    echo "  ❌ Server failed to start"
    echo "  Output: $OUTPUT"
    exit 1
fi
echo ""

# Test 3: Check VS Code configuration
echo "Test 3: Checking VS Code configuration..."
if [ -f "../../.vscode/settings.json" ]; then
    if grep -q "mcp.servers" "../../.vscode/settings.json"; then
        echo "  ✅ VS Code settings configured"
    else
        echo "  ⚠️  MCP server not found in VS Code settings"
    fi
else
    echo "  ⚠️  VS Code settings.json not found"
fi
echo ""

# Test 4: Check Copilot instructions
echo "Test 4: Checking Copilot instructions..."
if [ -f "../../.github/copilot-instructions.md" ]; then
    echo "  ✅ Copilot instructions file exists"
else
    echo "  ⚠️  Copilot instructions not found"
fi
echo ""

# Test 5: Check package dependencies
echo "Test 5: Checking dependencies..."
if [ -d "node_modules/@modelcontextprotocol" ]; then
    echo "  ✅ MCP SDK installed"
else
    echo "  ❌ MCP SDK not installed"
    exit 1
fi
echo ""

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║  ✅ All Tests Passed!                                    ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""
echo "📝 How to verify with Copilot:"
echo ""
echo "1. Restart VS Code (important!)"
echo "   - Press Ctrl+Shift+P"
echo "   - Type 'Developer: Reload Window'"
echo ""
echo "2. Open Copilot Chat and ask:"
echo "   💬 'Should I create a README in the components folder?'"
echo ""
echo "3. Expected response:"
echo "   ❌ 'No, do not create unnecessary README files'"
echo "   ✅ 'Update existing README.md or add to docs/'"
echo ""
echo "4. Try validation:"
echo "   💬 'I want to use npm link for shared packages'"
echo ""
echo "5. Expected response:"
echo "   ❌ 'Do not use npm link'"
echo "   ✅ 'Use file: protocol instead'"
echo ""
