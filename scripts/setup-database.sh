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
SAMPLE_DATA_DIR="$BASE_DIR/database/sample-data"

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
    print_status "Checking PostgreSQL Docker container..."
    if docker ps --filter "name=modern-reservation-postgres" --filter "status=running" | grep -q "modern-reservation-postgres"; then
        print_success "PostgreSQL Docker container is running"
        return 0
    else
        print_error "PostgreSQL Docker container is not running"
        return 1
    fi
}

# Function to check if Redis is running
check_redis() {
    print_status "Checking Redis Docker container..."
    if docker ps --filter "name=modern-reservation-redis" --filter "status=running" | grep -q "modern-reservation-redis"; then
        print_success "Redis Docker container is running"
        return 0
    else
        print_error "Redis Docker container is not running"
        return 1
    fi
}

# Function to check database connectivity
check_db_connectivity() {
    print_status "Testing database connectivity..."
    if docker exec modern-reservation-postgres psql -U postgres -d postgres -c "SELECT version();" > /dev/null 2>&1; then
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
    docker exec modern-reservation-postgres psql -U postgres -lqt | cut -d \| -f 1 | grep -qw "$db_name"
}

# Function to check if user exists
user_exists() {
    local username=$1
    docker exec modern-reservation-postgres psql -U postgres -t -c "SELECT 1 FROM pg_roles WHERE rolname='$username';" | grep -q 1
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
        docker exec modern-reservation-postgres psql -U postgres -c "CREATE USER $username WITH PASSWORD '$password';"
        print_success "User $username created"
    fi

    # Create database if not exists
    if database_exists "$db_name"; then
        print_warning "Database $db_name already exists"
    else
        print_status "Creating database: $db_name"
        docker exec modern-reservation-postgres createdb -U postgres -O "$username" "$db_name"
        print_success "Database $db_name created"
    fi

    # Grant privileges
    print_status "Granting privileges to $username on $db_name"
    docker exec modern-reservation-postgres psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE $db_name TO $username;"
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
    if docker exec -i modern-reservation-postgres psql -U postgres -d "$db_name" < "$file_path" > "$temp_output" 2>&1; then
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
    docker exec modern-reservation-postgres psql -U postgres -d "$db_name" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = '$table_name';" | grep -q 1
}

# Function to execute schema migration
execute_schema_migration() {
    local db_name=$1

    print_status "Executing schema migration for database: $db_name"

    # Schema files in order (updated to match actual files)
    local schema_files=(
        "01-extensions-and-types.sql"
        "02-core-tables.sql"
        "03-indexes.sql"
        "04-constraints.sql"
        "05-reference-data.sql"
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

# Function to execute sample data file
execute_sample_data_file() {
    local db_name=$1
    local data_file=$2
    local file_path="$SAMPLE_DATA_DIR/$data_file"

    if [ ! -f "$file_path" ]; then
        print_error "Sample data file not found: $file_path"
        return 1
    fi

    print_status "Loading sample data: $data_file"

    # Create a temporary output file to capture errors
    local temp_output=$(mktemp)

    # Execute the sample data file
    if docker exec -i modern-reservation-postgres psql -U postgres -d "$db_name" < "$file_path" > "$temp_output" 2>&1; then
        print_success "Sample data loaded: $data_file"
        rm -f "$temp_output"
        return 0
    else
        print_error "Failed to load sample data: $data_file"
        print_warning "Error details:"
        cat "$temp_output" | head -5
        rm -f "$temp_output"
        return 1
    fi
}

# Function to load sample data
load_sample_data() {
    local db_name=$1

    print_status "Loading sample data for database: $db_name"
    print_status "========================================="

    # Sample data files in order
    local sample_files=(
        "00-sample-system-user.sql"
        "01-sample-properties.sql"
        "02-sample-rooms.sql"
        "03-sample-guests.sql"
        "04-sample-users.sql"
        "05-sample-rate-plans.sql"
        "06-sample-reservations.sql"
        "07-sample-payments.sql"
        "08-sample-availability.sql"
        "09-sample-promotions.sql"
        "10-sample-reviews.sql"
    )

    local success_count=0
    local total_files=${#sample_files[@]}

    for data_file in "${sample_files[@]}"; do
        if execute_sample_data_file "$db_name" "$data_file"; then
            success_count=$((success_count + 1))
        else
            print_warning "Continuing despite error in $data_file..."
        fi
        sleep 1
    done

    print_status "========================================="
    if [ $success_count -eq $total_files ]; then
        print_success "All sample data files loaded successfully for $db_name"
        return 0
    else
        print_warning "Loaded $success_count out of $total_files sample data files for $db_name"
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

# Function to validate database setup
validate_database() {
    local db_name=$1

    print_status "========================================="
    print_status "DATABASE VALIDATION REPORT for: $db_name"
    print_status "========================================="

    # 1. Show all tables
    print_status "📋 All Tables in $db_name:"
    docker exec modern-reservation-postgres psql -U postgres -d "$db_name" -c "
        SELECT
            schemaname as schema,
            tablename as table_name,
            tableowner as owner,
            CASE
                WHEN tablename LIKE '%_2%' THEN 'Partition'
                ELSE 'Regular'
            END as table_type
        FROM pg_tables
        WHERE schemaname = 'public'
        ORDER BY
            CASE WHEN tablename LIKE '%_2%' THEN 1 ELSE 0 END,
            tablename;
    "

    # 2. Table count summary
    print_status "📊 Table Statistics:"
    docker exec modern-reservation-postgres psql -U postgres -d "$db_name" -c "
        SELECT
            'Total Tables' as metric,
            COUNT(*) as count
        FROM information_schema.tables
        WHERE table_schema = 'public'
        UNION ALL
        SELECT
            'Partitioned Tables' as metric,
            COUNT(*) as count
        FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name LIKE '%_2%'
        UNION ALL
        SELECT
            'Regular Tables' as metric,
            COUNT(*) as count
        FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name NOT LIKE '%_2%';
    "

    # 3. Foreign Key Relationships
    print_status "🔗 Foreign Key Relationships:"
    docker exec modern-reservation-postgres psql -U postgres -d "$db_name" -c "
        SELECT
            tc.table_name as from_table,
            kcu.column_name as from_column,
            ccu.table_name as to_table,
            ccu.column_name as to_column,
            tc.constraint_name
        FROM information_schema.table_constraints AS tc
        JOIN information_schema.key_column_usage AS kcu
            ON tc.constraint_name = kcu.constraint_name
            AND tc.table_schema = kcu.table_schema
        JOIN information_schema.constraint_column_usage AS ccu
            ON ccu.constraint_name = tc.constraint_name
            AND ccu.table_schema = tc.table_schema
        WHERE tc.constraint_type = 'FOREIGN KEY'
        AND tc.table_schema = 'public'
        ORDER BY tc.table_name, kcu.column_name;
    "

    # 4. Index Summary
    print_status "📚 Index Summary:"
    docker exec modern-reservation-postgres psql -U postgres -d "$db_name" -c "
        SELECT
            schemaname as schema,
            tablename as table_name,
            indexname as index_name,
            indexdef as index_definition
        FROM pg_indexes
        WHERE schemaname = 'public'
        AND indexname NOT LIKE '%pkey'
        ORDER BY tablename, indexname
        LIMIT 20;
    "

    # 5. Extensions and Types
    print_status "🔧 Extensions and Custom Types:"
    docker exec modern-reservation-postgres psql -U postgres -d "$db_name" -c "
        SELECT
            'Extensions' as category,
            extname as name,
            extversion as version
        FROM pg_extension
        WHERE extname NOT IN ('plpgsql')
        UNION ALL
        SELECT
            'Custom Types' as category,
            typname as name,
            '' as version
        FROM pg_type
        WHERE typnamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'public')
        AND typtype = 'e'
        ORDER BY category, name;
    "

    # 6. Partitioned Tables Details
    print_status "🗂️  Partitioned Tables Details:"
    docker exec modern-reservation-postgres psql -U postgres -d "$db_name" -c "
        SELECT
            schemaname,
            tablename as partition_name,
            pg_get_expr(c.relpartbound, c.oid) as partition_expression
        FROM pg_tables pt
        JOIN pg_class c ON c.relname = pt.tablename
        WHERE schemaname = 'public'
        AND tablename LIKE '%_2%'
        ORDER BY tablename;
    "

    print_status "✅ Database validation completed for: $db_name"
    print_status "========================================="
}

# Function to show help
show_help() {
    echo "Modern Reservation Database Setup Script"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -h, --help        Show this help message"
    echo "  -v, --validate    Only validate existing databases (no setup)"
    echo "  -d, --database    Specify database to validate (default: modern_reservation)"
    echo "  --debug           Load sample data (100+ rows per table) for testing"
    echo "  --clean           Drop and recreate databases before setup"
    echo ""
    echo "Examples:"
    echo "  $0                           # Full database setup"
    echo "  $0 --debug                   # Setup with sample data"
    echo "  $0 --clean --debug           # Clean install with sample data"
    echo "  $0 --validate                # Validate main database"
    echo "  $0 --validate -d modern_reservation_dev  # Validate dev database"
    echo ""
}

# Function to drop database if exists
drop_database_if_exists() {
    local db_name=$1

    if database_exists "$db_name"; then
        print_warning "Dropping existing database: $db_name"
        docker exec modern-reservation-postgres psql -U postgres -c "DROP DATABASE $db_name;"
        print_success "Database $db_name dropped"
    fi
}

# Main function
main() {
    local validate_only=false
    local target_database="modern_reservation"
    local load_sample_data=false
    local clean_install=false

    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -v|--validate)
                validate_only=true
                shift
                ;;
            -d|--database)
                target_database="$2"
                shift 2
                ;;
            --debug)
                load_sample_data=true
                shift
                ;;
            --clean)
                clean_install=true
                shift
                ;;
            *)
                print_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done

    if [ "$validate_only" = true ]; then
        print_status "Running Database Validation Only"
        print_status "================================="

        # Check prerequisites
        if ! check_postgresql || ! check_db_connectivity; then
            print_error "Prerequisites check failed"
            exit 1
        fi

        validate_database "$target_database"
        exit 0
    fi

    print_status "Starting Modern Reservation Database Setup"
    print_status "========================================="

    if [ "$load_sample_data" = true ]; then
        print_status "🎲 DEBUG MODE: Sample data will be loaded"
    fi

    if [ "$clean_install" = true ]; then
        print_warning "🧹 CLEAN INSTALL: Existing databases will be dropped"
    fi

    print_status ""

    # Check prerequisites
    if ! check_postgresql || ! check_redis || ! check_db_connectivity; then
        print_error "Prerequisites check failed"
        exit 1
    fi

    print_status "Prerequisites check passed"
    print_status ""

    # Clean install if requested
    if [ "$clean_install" = true ]; then
        print_status "Performing clean installation..."
        drop_database_if_exists "modern_reservation"
        drop_database_if_exists "modern_reservation_dev"
        drop_database_if_exists "modern_reservation_payments"
        print_status ""
    fi

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
        print_status ""

        # Load sample data if --debug flag is set
        if [ "$load_sample_data" = true ]; then
            print_status ""
            print_status "🎲 Loading sample data (this may take a few minutes)..."
            load_sample_data "modern_reservation"
            print_status ""
        fi

        validate_database "modern_reservation"
    fi

    if execute_schema_migration "modern_reservation_dev"; then
        verify_schema "modern_reservation_dev"

        # Load sample data for dev database if --debug flag is set
        if [ "$load_sample_data" = true ]; then
            print_status ""
            print_status "🎲 Loading sample data for dev database..."
            load_sample_data "modern_reservation_dev"
            print_status ""
        fi
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

    if [ "$load_sample_data" = true ]; then
        print_status "🎲 Sample Data Summary:"
        print_status "  • Properties: 10 hotels with amenities"
        print_status "  • Rooms: 150 rooms across properties"
        print_status "  • Guests: 100 guests with profiles"
        print_status "  • Users: 50 users with roles"
        print_status "  • Rates: 100+ rates for testing"
        print_status "  • Reservations: 150 bookings"
        print_status "  • Payments: 120 payment records"
        print_status "  • Reviews: 80 guest reviews"
        print_status ""
    fi

    print_status "Redis Server: Running on localhost:6379"
    print_status "PostgreSQL:   Running on localhost:5432"
    print_status ""
    print_success "Your business services should now be able to connect to the databases!"
    print_status ""
    print_status "💡 Useful commands:"
    print_status "   $0 --validate                    # Validate main database"
    print_status "   $0 --validate -d <database_name> # Validate specific database"
    print_status "   $0 --clean --debug               # Fresh install with sample data"
}

# Trap to handle script interruption
cleanup() {
    print_warning "Database setup interrupted"
    exit 1
}

trap cleanup SIGINT SIGTERM

# Run main function
main "$@"
