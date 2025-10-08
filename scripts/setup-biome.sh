#!/bin/bash
# filepath: scripts/setup-biome.sh

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

print_status() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }

echo -e "${CYAN}════════════════════════════════════════════════════════════════${NC}"
echo -e "${CYAN}                Setting up Biome.js across workspace              ${NC}"
echo -e "${CYAN}════════════════════════════════════════════════════════════════${NC}"
echo ""

# Track processed applications
PROCESSED_APPS=()
FAILED_APPS=()

# Function to create biome config for different project types
create_biome_config() {
    local project_path="$1"
    local project_type="${2:-default}"
    
    local config_file="$project_path/biome.json"
    
    case "$project_type" in
        "nextjs")
            cat > "$config_file" << 'EOF'
{
  "$schema": "https://biomejs.dev/schemas/2.2.0/schema.json",
  "vcs": {
    "enabled": true,
    "clientKind": "git",
    "useIgnoreFile": true
  },
  "files": {
    "ignoreUnknown": true,
    "includes": ["src/**/*", "pages/**/*", "components/**/*", "lib/**/*", "utils/**/*", "hooks/**/*", "types/**/*"],
    "ignore": ["node_modules/**", ".next/**", "dist/**", "build/**", "coverage/**", ".turbo/**"]
  },
  "formatter": {
    "enabled": true,
    "indentStyle": "space",
    "indentWidth": 2,
    "lineWidth": 100
  },
  "linter": {
    "enabled": true,
    "rules": {
      "recommended": true,
      "suspicious": {
        "noUnknownAtRules": "off"
      },
      "correctness": {
        "noUnusedVariables": "error",
        "useExhaustiveDependencies": "warn"
      },
      "style": {
        "noNegationElse": "off",
        "useConst": "error"
      }
    },
    "domains": {
      "next": "recommended",
      "react": "recommended"
    }
  },
  "javascript": {
    "formatter": {
      "quoteStyle": "single",
      "trailingComma": "es5",
      "semicolons": "always"
    }
  },
  "assist": {
    "actions": {
      "source": {
        "organizeImports": "on"
      }
    }
  }
}
EOF
            ;;
            
        "nodejs-service")
            cat > "$config_file" << 'EOF'
{
  "$schema": "https://biomejs.dev/schemas/2.2.0/schema.json",
  "vcs": {
    "enabled": true,
    "clientKind": "git",
    "useIgnoreFile": true
  },
  "files": {
    "ignoreUnknown": true,
    "includes": ["src/**/*", "lib/**/*", "utils/**/*", "types/**/*", "**/*.ts", "**/*.js"],
    "ignore": ["node_modules/**", "dist/**", "build/**", "coverage/**", ".turbo/**", "*.d.ts"]
  },
  "formatter": {
    "enabled": true,
    "indentStyle": "space",
    "indentWidth": 2,
    "lineWidth": 100
  },
  "linter": {
    "enabled": true,
    "rules": {
      "recommended": true,
      "correctness": {
        "noUnusedVariables": "error"
      },
      "style": {
        "noNegationElse": "off",
        "useConst": "error"
      },
      "suspicious": {
        "noConsoleLog": "warn"
      }
    }
  },
  "javascript": {
    "formatter": {
      "quoteStyle": "single",
      "trailingComma": "es5",
      "semicolons": "always"
    }
  },
  "assist": {
    "actions": {
      "source": {
        "organizeImports": "on"
      }
    }
  }
}
EOF
            ;;
            
        "shared-lib")
            cat > "$config_file" << 'EOF'
{
  "$schema": "https://biomejs.dev/schemas/2.2.0/schema.json",
  "vcs": {
    "enabled": true,
    "clientKind": "git",
    "useIgnoreFile": true
  },
  "files": {
    "ignoreUnknown": true,
    "includes": ["src/**/*", "lib/**/*", "index.ts", "**/*.ts", "**/*.js"],
    "ignore": ["node_modules/**", "dist/**", "build/**", "coverage/**", ".turbo/**", "*.d.ts"]
  },
  "formatter": {
    "enabled": true,
    "indentStyle": "space",
    "indentWidth": 2,
    "lineWidth": 100
  },
  "linter": {
    "enabled": true,
    "rules": {
      "recommended": true,
      "correctness": {
        "noUnusedVariables": "error"
      },
      "style": {
        "noNegationElse": "off",
        "useConst": "error"
      }
    }
  },
  "javascript": {
    "formatter": {
      "quoteStyle": "single",
      "trailingComma": "es5",
      "semicolons": "always"
    }
  },
  "assist": {
    "actions": {
      "source": {
        "organizeImports": "on"
      }
    }
  }
}
EOF
            ;;
            
        "workspace-root")
            cat > "$config_file" << 'EOF'
{
  "$schema": "https://biomejs.dev/schemas/2.2.0/schema.json",
  "vcs": {
    "enabled": true,
    "clientKind": "git",
    "useIgnoreFile": true
  },
  "files": {
    "ignoreUnknown": true,
    "includes": ["apps/**/*", "libs/**/*", "scripts/**/*", "tools/**/*", "*.ts", "*.js", "*.json"],
    "ignore": [
      "node_modules/**", 
      "**/node_modules/**",
      "**/dist/**", 
      "**/build/**", 
      "**/.next/**",
      "**/coverage/**", 
      "**/.turbo/**", 
      "**/*.d.ts",
      "apps/backend/java-services/**"
    ]
  },
  "formatter": {
    "enabled": true,
    "indentStyle": "space",
    "indentWidth": 2,
    "lineWidth": 100
  },
  "linter": {
    "enabled": true,
    "rules": {
      "recommended": true,
      "correctness": {
        "noUnusedVariables": "error"
      },
      "style": {
        "noNegationElse": "off",
        "useConst": "error"
      }
    }
  },
  "javascript": {
    "formatter": {
      "quoteStyle": "single",
      "trailingComma": "es5",
      "semicolons": "always"
    }
  },
  "assist": {
    "actions": {
      "source": {
        "organizeImports": "on"
      }
    }
  }
}
EOF
            ;;
    esac
    
    print_success "Created biome.json for $project_path ($project_type)"
}

# Function to add biome scripts to package.json
add_biome_scripts() {
    local package_json="$1"
    
    if [ ! -f "$package_json" ]; then
        print_warning "package.json not found in $package_json"
        return 1
    fi
    
    # Check if jq is available for JSON manipulation
    if ! command -v jq &> /dev/null; then
        print_error "jq is not installed. Installing..."
        sudo apt install -y jq
    fi
    
    # Backup original package.json
    cp "$package_json" "$package_json.backup"
    
    # Add biome scripts
    jq '.scripts += {
        "format": "biome format --write .",
        "format:check": "biome format .",
        "lint": "biome lint .",
        "lint:fix": "biome lint --write .",
        "check": "biome check .",
        "check:fix": "biome check --write ."
    }' "$package_json.backup" > "$package_json"
    
    if [ $? -eq 0 ]; then
        rm "$package_json.backup"
        print_success "Added biome scripts to $package_json"
    else
        mv "$package_json.backup" "$package_json"
        print_error "Failed to update $package_json"
        return 1
    fi
}

# Install Biome globally if not already installed
print_status "Checking Biome installation..."
if ! command -v biome &> /dev/null; then
    print_status "Installing Biome globally..."
    sudo npm install -g @biomejs/biome
    if [ $? -eq 0 ]; then
        print_success "Biome installed globally"
    else
        print_error "Failed to install Biome globally"
        exit 1
    fi
else
    print_success "Biome already installed"
fi

# Install Biome in root workspace
print_status "Installing Biome in root workspace..."
if [ -f "package.json" ]; then
    if ! grep -q "@biomejs/biome" package.json; then
        npm install --save-dev @biomejs/biome
        print_success "Added Biome to root workspace"
    else
        print_success "Biome already in root workspace"
    fi
    
    # Create workspace root biome config
    if [ ! -f "biome.json" ]; then
        create_biome_config "." "workspace-root"
        add_biome_scripts "package.json"
        PROCESSED_APPS+=("Root Workspace")
    else
        print_success "Root biome.json already exists"
        PROCESSED_APPS+=("Root Workspace (existing)")
    fi
fi

# Process Frontend Applications
print_status "Processing frontend applications..."

# Guest Portal (Next.js)
GUEST_PORTAL="apps/frontend/guest-portal"
if [ -d "$GUEST_PORTAL" ] && [ -f "$GUEST_PORTAL/package.json" ]; then
    if [ -f "$GUEST_PORTAL/biome.json" ]; then
        print_success "$GUEST_PORTAL already has Biome configured"
        PROCESSED_APPS+=("Guest Portal (existing)")
    else
        print_status "Setting up Biome for Guest Portal..."
        cd "$GUEST_PORTAL"
        npm install --save-dev @biomejs/biome
        cd - > /dev/null
        
        create_biome_config "$GUEST_PORTAL" "nextjs"
        add_biome_scripts "$GUEST_PORTAL/package.json"
        PROCESSED_APPS+=("Guest Portal")
    fi
fi

# Process Backend Node Services
print_status "Processing backend Node.js services..."

NODE_SERVICES=(
    "api-gateway"
    "audit-service"
    "channel-manager"
    "file-upload-service"
    "housekeeping-service"
    "notification-service"
    "websocket-service"
)

for service in "${NODE_SERVICES[@]}"; do
    SERVICE_PATH="apps/backend/node-services/$service"
    
    if [ -d "$SERVICE_PATH" ]; then
        if [ -f "$SERVICE_PATH/.gitkeep" ] && [ ! -f "$SERVICE_PATH/package.json" ]; then
            print_warning "$service appears to be empty (only .gitkeep found)"
            continue
        fi
        
        print_status "Setting up Biome for $service..."
        
        # Create package.json if it doesn't exist
        if [ ! -f "$SERVICE_PATH/package.json" ]; then
            cat > "$SERVICE_PATH/package.json" << EOF
{
  "name": "@modern-reservation/$service",
  "version": "1.0.0",
  "private": true,
  "main": "dist/index.js",
  "scripts": {
    "build": "tsc",
    "dev": "ts-node src/index.ts",
    "start": "node dist/index.js"
  },
  "devDependencies": {
    "@biomejs/biome": "^1.9.4",
    "@types/node": "^20.8.0",
    "typescript": "^5.2.0",
    "ts-node": "^10.9.0"
  },
  "dependencies": {
    "@modern-reservation/schemas": "file:../../../libs/shared/schemas"
  }
}
EOF
            print_success "Created package.json for $service"
        fi
        
        # Install Biome in the service
        if ! grep -q "@biomejs/biome" "$SERVICE_PATH/package.json"; then
            cd "$SERVICE_PATH"
            npm install --save-dev @biomejs/biome
            cd - > /dev/null
        fi
        
        create_biome_config "$SERVICE_PATH" "nodejs-service"
        add_biome_scripts "$SERVICE_PATH/package.json"
        PROCESSED_APPS+=("$service")
    fi
done

# Process Shared Libraries
print_status "Processing shared libraries..."

SHARED_LIBS=(
    "backend-utils"
    "constants"
    "graphql-client"
    "graphql-schemas"
    "schemas"
    "tenant-commons"
    "testing-utils"
    "ui-components"
)

for lib in "${SHARED_LIBS[@]}"; do
    LIB_PATH="libs/shared/$lib"
    
    if [ -d "$LIB_PATH" ] && [ -f "$LIB_PATH/package.json" ]; then
        print_status "Setting up Biome for shared/$lib..."
        
        # Install Biome in the library
        if ! grep -q "@biomejs/biome" "$LIB_PATH/package.json"; then
            cd "$LIB_PATH"
            npm install --save-dev @biomejs/biome
            cd - > /dev/null
        fi
        
        create_biome_config "$LIB_PATH" "shared-lib"
        add_biome_scripts "$LIB_PATH/package.json"
        PROCESSED_APPS+=("shared/$lib")
    elif [ -d "$LIB_PATH" ]; then
        print_warning "$lib exists but has no package.json - skipping"
    fi
done

# Create workspace-wide biome scripts
print_status "Creating workspace-wide Biome scripts..."

# Summary
echo ""
echo -e "${CYAN}════════════════════════════════════════════════════════════════${NC}"
echo -e "${CYAN}                       Setup Summary                             ${NC}"
echo -e "${CYAN}════════════════════════════════════════════════════════════════${NC}"

print_success "Biome setup completed!"
echo ""
echo -e "${GREEN}✅ Processed Applications (${#PROCESSED_APPS[@]} total):${NC}"
for app in "${PROCESSED_APPS[@]}"; do
    echo "   • $app"
done

if [ ${#FAILED_APPS[@]} -gt 0 ]; then
    echo ""
    echo -e "${RED}❌ Failed Applications:${NC}"
    for app in "${FAILED_APPS[@]}"; do
        echo "   • $app"
    done
fi

echo ""
echo -e "${YELLOW}📋 Available Commands:${NC}"
echo ""
echo -e "${BLUE}Direct biome commands:${NC}"
echo "  biome format --write .   - Format entire workspace"
echo "  biome lint .             - Lint entire workspace"
echo "  biome check --write .    - Check and fix entire workspace"
echo ""
echo -e "${CYAN}════════════════════════════════════════════════════════════════${NC}"