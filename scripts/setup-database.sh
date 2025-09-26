#!/bin/bash

# Modern Reservation System - Database Setup Script
# This script sets up PostgreSQL databases and executes schema migrations

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SCHEMA_DIR="$BASE_DIR/database/schema"

# Print functions
print_status() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')] $1${NC}"
}

print_success() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] ✅ $1${NC}"
}

print_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] ⚠️  $1${NC}"
}

# Function to check if PostgreSQL is running
check_postgresql() {
    print_status "Checking PostgreSQL service..."
    if sudo systemctl is-active --quiet postgresql; then
        print_success "PostgreSQL service is running"
        return 0
    else
        print_error "PostgreSQL service is not running"
        return 1
    fi
}

# Function to check if Redis is running
check_redis() {
    print_status "Checking Redis service..."
    if sudo systemctl is-active --quiet redis-server; then
        print_success "Redis service is running"
        return 0
    else
        print_error "Redis service is not running"
        return 1
    fi
}

# Function to check database connectivity
check_db_connectivity() {
    print_status "Testing database connectivity..."
    if sudo -u postgres psql -d postgres -c "SELECT version();" > /dev/null 2>&1; then
        print_success "Database connectivity confirmed"
        return 0
    else
        print_error "Cannot connect to database"
        return 1
    fi
}

# Function to check if database exists
database_exists() {
    local db_name=$1
    sudo -u postgres psql -lqt | cut -d \| -f 1 | grep -qw "$db_name"
}

# Function to check if user exists
user_exists() {
    local username=$1
    sudo -u postgres psql -t -c "SELECT 1 FROM pg_roles WHERE rolname='$username';" | grep -q 1
}

# Function to create database and user
setup_database() {
    local db_name=$1
    local username=$2
    local password=$3

    print_status "Setting up database: $db_name with user: $username"

    # Create user if not exists
    if user_exists "$username"; then
        print_warning "User $username already exists"
    else
        print_status "Creating user: $username"
        sudo -u postgres psql -c "CREATE USER $username WITH PASSWORD '$password';"
        print_success "User $username created"
    fi

    # Create database if not exists
    if database_exists "$db_name"; then
        print_warning "Database $db_name already exists"
    else
        print_status "Creating database: $db_name"
        sudo -u postgres createdb -O "$username" "$db_name"
        print_success "Database $db_name created"
    fi

    # Grant privileges
    print_status "Granting privileges to $username on $db_name"
    sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE $db_name TO $username;"
    print_success "Privileges granted"
}

# Function to execute schema file
execute_schema_file() {
    local db_name=$1
    local schema_file=$2
    local file_path="$SCHEMA_DIR/$schema_file"

    if [ ! -f "$file_path" ]; then
        print_error "Schema file not found: $file_path"
        return 1
    fi

    print_status "Executing schema file: $schema_file on database: $db_name"

    # Create a temporary output file to capture errors
    local temp_output=$(mktemp)

    # Use non-interactive mode with longer timeout and capture output
    if sudo -u postgres psql -d "$db_name" -f "$file_path" > "$temp_output" 2>&1; then
        print_success "Schema file executed: $schema_file"
        rm -f "$temp_output"
        return 0
    else
        print_error "Failed to execute schema file: $schema_file"
        print_warning "Error details:"
        cat "$temp_output" | head -5
        rm -f "$temp_output"
        return 1
    fi
}

# Function to check if table exists
table_exists() {
    local db_name=$1
    local table_name=$2
    sudo -u postgres psql -d "$db_name" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = '$table_name';" | grep -q 1
}

# Function to execute schema migration
execute_schema_migration() {
    local db_name=$1

    print_status "Executing schema migration for database: $db_name"

    # Schema files in order
    local schema_files=(
        "00-extensions-and-types.sql"
        "01-property-management.sql"
        "02-guest-management.sql"
        "03-reservation-management.sql"
        "04-payment-management.sql"
        "05-availability-rate-management.sql"
        "06-user-management.sql"
        "07-audit-and-events.sql"
        "08-notifications.sql"
    )

    local success_count=0
    local total_files=${#schema_files[@]}

    for schema_file in "${schema_files[@]}"; do
        if execute_schema_file "$db_name" "$schema_file"; then
            success_count=$((success_count + 1))
        fi
    done

    if [ $success_count -eq $total_files ]; then
        print_success "All schema files executed successfully for $db_name"
        return 0
    else
        print_error "Only $success_count out of $total_files schema files executed successfully for $db_name"
        return 1
    fi
}

# Function to verify schema
verify_schema() {
    local db_name=$1

    print_status "Verifying schema for database: $db_name"

    # Check for some key tables
    local key_tables=("properties" "guests" "reservations" "payments" "users")
    local found_tables=0

    for table in "${key_tables[@]}"; do
        if table_exists "$db_name" "$table"; then
            print_success "Table exists: $table"
            found_tables=$((found_tables + 1))
        else
            print_warning "Table missing: $table"
        fi
    done

    if [ $found_tables -eq ${#key_tables[@]} ]; then
        print_success "Schema verification passed for $db_name"
        return 0
    else
        print_warning "Schema verification partially passed: $found_tables/${#key_tables[@]} tables found"
        return 1
    fi
}

# Main function
main() {
    print_status "Starting Modern Reservation Database Setup"
    print_status "========================================="

    # Check prerequisites
    if ! check_postgresql || ! check_redis || ! check_db_connectivity; then
        print_error "Prerequisites check failed"
        exit 1
    fi

    print_status "Prerequisites check passed"
    print_status ""

    # Setup databases
    print_status "Setting up databases..."

    setup_database "modern_reservation" "reservation_user" "reservation_pass"
    setup_database "modern_reservation_dev" "reservation_user" "reservation_pass"
    setup_database "modern_reservation_payments" "modern_reservation" "password"

    print_status ""
    print_status "Executing schema migrations..."

    # Execute schema migrations
    if execute_schema_migration "modern_reservation"; then
        verify_schema "modern_reservation"
    fi

    if execute_schema_migration "modern_reservation_dev"; then
        verify_schema "modern_reservation_dev"
    fi

    # Payment database might have different schema
    print_status "Setting up payment database schema..."
    execute_schema_file "modern_reservation_payments" "04-payment-management.sql"

    print_status ""
    print_status "========================================="
    print_success "Database setup completed!"
    print_status ""
    print_status "Database Summary:"
    print_status "  • Main Database: modern_reservation (user: reservation_user)"
    print_status "  • Dev Database:  modern_reservation_dev (user: reservation_user)"
    print_status "  • Payment DB:    modern_reservation_payments (user: modern_reservation)"
    print_status ""
    print_status "Redis Server: Running on localhost:6379"
    print_status "PostgreSQL:   Running on localhost:5432"
    print_status ""
    print_success "Your business services should now be able to connect to the databases!"
}

# Trap to handle script interruption
cleanup() {
    print_warning "Database setup interrupted"
    exit 1
}

trap cleanup SIGINT SIGTERM

# Run main function
main "$@"
