#!/bin/bash
# filepath: scripts/biome-management.sh

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

print_status() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }

show_help() {
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}                   Biome Management Script                      ${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    echo "Usage: ./scripts/biome-management.sh <command> [options]"
    echo ""
    echo -e "${YELLOW}Commands:${NC}"
    echo ""
    echo -e "${GREEN}Setup & Installation:${NC}"
    echo "  setup            - Initial setup of Biome across all projects"
    echo "  install-missing  - Install Biome in projects that don't have it"
    echo "  update-configs   - Update all biome.json configs"
    echo ""
    echo -e "${GREEN}Formatting & Linting:${NC}"
    echo "  format-all       - Format all Node.js projects"
    echo "  format-check     - Check formatting without applying changes"
    echo "  lint-all         - Lint all Node.js projects"
    echo "  lint-fix         - Lint and fix all issues automatically"
    echo "  check-all        - Run both format and lint checks"
    echo "  fix-all          - Run format and lint with auto-fix"
    echo ""
    echo -e "${GREEN}Project-specific Operations:${NC}"
    echo "  format-project <path>  - Format specific project"
    echo "  lint-project <path>    - Lint specific project"
    echo "  check-project <path>   - Check specific project"
    echo ""
    echo -e "${GREEN}Reporting & Status:${NC}"
    echo "  status           - Show Biome installation status across projects"
    echo "  report           - Generate detailed formatting/linting report"
    echo "  list-projects    - List all Node.js projects with Biome"
    echo ""
    echo -e "${GREEN}Utilities:${NC}"
    echo "  help             - Show this help message"
    echo ""
    echo -e "${BLUE}Examples:${NC}"
    echo "  ./scripts/biome-management.sh setup"
    echo "  ./scripts/biome-management.sh format-all"
    echo "  ./scripts/biome-management.sh format-project apps/frontend/guest-portal"
    echo "  ./scripts/biome-management.sh status"
    echo ""
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}"
}

# Function to check if Biome is installed in a directory
check_biome_installed() {
    local dir="$1"
    if [ -f "$dir/package.json" ] && grep -q "@biomejs/biome" "$dir/package.json"; then
        return 0
    fi
    return 1
}

# Function to get all Node.js projects
get_nodejs_projects() {
    local projects=()
    
    # Add root if it has package.json
    if [ -f "package.json" ]; then
        projects+=(".")
    fi
    
    # Frontend applications
    if [ -d "apps/frontend/guest-portal" ] && [ -f "apps/frontend/guest-portal/package.json" ]; then
        projects+=("apps/frontend/guest-portal")
    fi
    
    # Backend Node services
    for service in api-gateway audit-service channel-manager file-upload-service housekeeping-service notification-service websocket-service; do
        if [ -d "apps/backend/node-services/$service" ] && [ -f "apps/backend/node-services/$service/package.json" ]; then
            projects+=("apps/backend/node-services/$service")
        fi
    done
    
    # Shared libraries
    for lib in backend-utils constants graphql-client graphql-schemas schemas tenant-commons testing-utils ui-components; do
        if [ -d "libs/shared/$lib" ] && [ -f "libs/shared/$lib/package.json" ]; then
            projects+=("libs/shared/$lib")
        fi
    done
    
    printf '%s\n' "${projects[@]}"
}

# Function to run Biome command on all projects
run_biome_on_all() {
    local command="$1"
    local description="$2"
    local projects=($(get_nodejs_projects))
    local failed_projects=()
    
    print_status "$description"
    echo ""
    
    for project in "${projects[@]}"; do
        print_status "Processing: $project"
        
        if [ -f "$project/biome.json" ]; then
            cd "$project" || continue
            
            if eval "$command"; then
                print_success "✅ $project"
            else
                print_error "❌ $project - Failed"
                failed_projects+=("$project")
            fi
            
            cd - > /dev/null
        else
            print_warning "⚠️  $project - No biome.json found"
            failed_projects+=("$project")
        fi
    done
    
    echo ""
    if [ ${#failed_projects[@]} -eq 0 ]; then
        print_success "All projects processed successfully!"
    else
        print_warning "Failed projects: ${failed_projects[*]}"
    fi
}

# Function to run Biome command on specific project
run_biome_on_project() {
    local project_path="$1"
    local command="$2"
    local description="$3"
    
    if [ ! -d "$project_path" ]; then
        print_error "Project directory not found: $project_path"
        return 1
    fi
    
    if [ ! -f "$project_path/package.json" ]; then
        print_error "No package.json found in: $project_path"
        return 1
    fi
    
    if [ ! -f "$project_path/biome.json" ]; then
        print_error "No biome.json found in: $project_path"
        return 1
    fi
    
    print_status "$description: $project_path"
    
    cd "$project_path" || return 1
    
    if eval "$command"; then
        print_success "✅ Completed successfully"
    else
        print_error "❌ Failed"
        return 1
    fi
    
    cd - > /dev/null
}

# Function to show status of Biome installation
show_status() {
    local projects=($(get_nodejs_projects))
    
    print_status "Biome Installation Status"
    echo ""
    
    printf "%-50s %-15s %-15s\n" "Project" "Biome Installed" "Config Exists"
    echo "────────────────────────────────────────────────────────────────────────────────"
    
    for project in "${projects[@]}"; do
        local biome_installed="❌"
        local config_exists="❌"
        
        if check_biome_installed "$project"; then
            biome_installed="✅"
        fi
        
        if [ -f "$project/biome.json" ]; then
            config_exists="✅"
        fi
        
        printf "%-50s %-15s %-15s\n" "$project" "$biome_installed" "$config_exists"
    done
    
    echo ""
}

# Function to generate detailed report
generate_report() {
    local projects=($(get_nodejs_projects))
    local report_file="biome-report-$(date +%Y%m%d-%H%M%S).txt"
    
    print_status "Generating detailed Biome report..."
    
    {
        echo "Biome Report - Generated on $(date)"
        echo "=============================================="
        echo ""
        
        for project in "${projects[@]}"; do
            echo "Project: $project"
            echo "$(printf '=%.0s' {1..50})"
            
            if [ -f "$project/biome.json" ]; then
                cd "$project" || continue
                
                echo ""
                echo "Format Check:"
                biome format . 2>&1 || echo "Format check failed"
                
                echo ""
                echo "Lint Check:"
                biome lint . 2>&1 || echo "Lint check failed"
                
                cd - > /dev/null
            else
                echo "No biome.json configuration found"
            fi
            
            echo ""
            echo ""
        done
    } > "$report_file"
    
    print_success "Report generated: $report_file"
}

# Function to list all projects
list_projects() {
    local projects=($(get_nodejs_projects))
    
    print_status "Node.js Projects with Package.json"
    echo ""
    
    for project in "${projects[@]}"; do
        local status=""
        if [ -f "$project/biome.json" ]; then
            status="(Biome configured)"
        else
            status="(No Biome config)"
        fi
        
        echo "  • $project $status"
    done
    
    echo ""
    print_status "Total projects: ${#projects[@]}"
}

# Main script logic
case "$1" in
    "setup")
        print_status "Running initial Biome setup..."
        ./scripts/setup-biome.sh
        ;;
    "format-all")
        run_biome_on_all "biome format --write ." "Formatting all projects"
        ;;
    "format-check")
        run_biome_on_all "biome format ." "Checking formatting on all projects"
        ;;
    "lint-all")
        run_biome_on_all "biome lint ." "Linting all projects"
        ;;
    "lint-fix")
        run_biome_on_all "biome lint --write ." "Linting and fixing all projects"
        ;;
    "check-all")
        run_biome_on_all "biome check ." "Checking all projects (format + lint)"
        ;;
    "fix-all")
        run_biome_on_all "biome check --write ." "Fixing all projects (format + lint)"
        ;;
    "format-project")
        if [ -z "$2" ]; then
            print_error "Please specify project path"
            exit 1
        fi
        run_biome_on_project "$2" "biome format --write ." "Formatting project"
        ;;
    "lint-project")
        if [ -z "$2" ]; then
            print_error "Please specify project path"
            exit 1
        fi
        run_biome_on_project "$2" "biome lint ." "Linting project"
        ;;
    "check-project")
        if [ -z "$2" ]; then
            print_error "Please specify project path"
            exit 1
        fi
        run_biome_on_project "$2" "biome check --write ." "Checking and fixing project"
        ;;
    "install-missing")
        print_status "Installing Biome in projects that need it..."
        ./scripts/setup-biome.sh
        ;;
    "update-configs")
        print_status "Updating Biome configurations..."
        ./scripts/setup-biome.sh
        ;;
    "status")
        show_status
        ;;
    "report")
        generate_report
        ;;
    "list-projects")
        list_projects
        ;;
    *)
        show_help
        ;;
esac