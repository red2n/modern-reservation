#!/bin/bash

# GraphQL Schema Composition Script
# Combines all modular GraphQL files into a single composed schema

set -e

SCHEMA_DIR="$(dirname "$0")/types"
OUTPUT_FILE="$(dirname "$0")/composed-schema.graphql"
TEMP_FILE="$(dirname "$0")/.temp-schema.graphql"

echo "🔧 Composing GraphQL schema from modular files..."

# Clear output files
> "$OUTPUT_FILE"
> "$TEMP_FILE"

# Header comment
cat << 'EOF' > "$OUTPUT_FILE"
# Composed GraphQL Schema
# Auto-generated from modular schema files
# DO NOT EDIT DIRECTLY - Edit individual files in types/ directory

EOF

echo "📁 Loading schema files in dependency order..."

# Load files in correct dependency order
FILES=(
    "common.graphql"
    "tenant.graphql"
    "property.graphql"
    "guest.graphql"
    "user.graphql"
    "reservation.graphql"
    "payment.graphql"
    "availability.graphql"
    "analytics.graphql"
    "housekeeping.graphql"
    "channel.graphql"
    "review.graphql"
)

for file in "${FILES[@]}"; do
    file_path="$SCHEMA_DIR/$file"
    if [ -f "$file_path" ]; then
        echo "  ✅ Loading $file"
        echo "" >> "$OUTPUT_FILE"
        echo "# =============================================================================" >> "$OUTPUT_FILE"
        echo "# $(basename "$file" .graphql | tr '[:lower:]' '[:upper:]') SCHEMA" >> "$OUTPUT_FILE"
        echo "# =============================================================================" >> "$OUTPUT_FILE"
        echo "" >> "$OUTPUT_FILE"
        cat "$file_path" >> "$OUTPUT_FILE"
    else
        echo "  ⚠️  Warning: $file not found, skipping..."
    fi
done

# Add root types at the end
echo "" >> "$OUTPUT_FILE"
echo "# =============================================================================" >> "$OUTPUT_FILE"
echo "# ROOT TYPES" >> "$OUTPUT_FILE"
echo "# =============================================================================" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

cat << 'EOF' >> "$OUTPUT_FILE"
# Define root types if not already defined
type Query {
  # Health check
  _service: _Service!
}

type Mutation {
  # Placeholder
  _empty: String
}

type Subscription {
  # Placeholder
  _empty: String
}

type _Service {
  sdl: String!
}
EOF

echo "✅ Schema composition completed!"
echo "📄 Output file: $OUTPUT_FILE"

# Validate schema if GraphQL CLI is available
if command -v graphql &> /dev/null; then
    echo "🔍 Validating composed schema..."
    if graphql validate "$OUTPUT_FILE"; then
        echo "✅ Schema validation passed!"
    else
        echo "❌ Schema validation failed!"
        exit 1
    fi
else
    echo "⚠️  GraphQL CLI not found, skipping validation"
    echo "   Install with: npm install -g @graphql-cli/cli"
fi

# Generate TypeScript types if GraphQL Code Generator is available
if command -v graphql-codegen &> /dev/null; then
    echo "🔄 Generating TypeScript types..."
    if [ -f "$(dirname "$0")/codegen.yml" ]; then
        cd "$(dirname "$0")"
        graphql-codegen
        echo "✅ TypeScript types generated!"
    else
        echo "⚠️  codegen.yml not found, skipping type generation"
    fi
else
    echo "⚠️  GraphQL Code Generator not found, skipping type generation"
    echo "   Install with: npm install -g @graphql-codegen/cli"
fi

echo "🎉 Schema composition complete!"
echo ""
echo "Next steps:"
echo "1. Review the composed schema: $OUTPUT_FILE"
echo "2. Test with your GraphQL gateway"
echo "3. Deploy to your federated services"
