#!/bin/bash

# Enhanced Database Backup and Restore Script
# Provides comprehensive database management capabilities

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKUP_DIR="$BASE_DIR/backups/database"

# Load environment variables if available
if [ -f "$BASE_DIR/.env" ]; then
    source "$BASE_DIR/.env"
fi

# Database configuration
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-modern_reservation}
DB_USER=${DB_USERNAME:-reservation_user}
export PGPASSWORD="${DB_PASSWORD}"

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

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Function to create database backup
backup_database() {
    local backup_name=${1:-"manual_backup_$(date +%Y%m%d_%H%M%S)"}
    local backup_file="$BACKUP_DIR/${backup_name}.sql"
    local compressed_file="$backup_file.gz"

    print_status "Creating database backup: $backup_name"

    # Check if database exists
    if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -lqt | cut -d \| -f 1 | grep -qw "$DB_NAME"; then
        print_error "Database $DB_NAME does not exist"
        return 1
    fi

    # Create backup
    if pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
        --format=plain \
        --no-owner \
        --no-acl \
        --verbose \
        --file="$backup_file" 2>&1 | grep -v "^$"; then

        # Compress backup
        print_status "Compressing backup..."
        gzip -f "$backup_file"

        # Get file size
        local size=$(du -h "$compressed_file" | cut -f1)

        print_success "Backup created successfully: $compressed_file ($size)"

        # Create metadata file
        cat > "$BACKUP_DIR/${backup_name}.meta" <<EOF
Backup Name: $backup_name
Database: $DB_NAME
Host: $DB_HOST:$DB_PORT
User: $DB_USER
Created: $(date '+%Y-%m-%d %H:%M:%S')
Size: $size
Type: Full Database Backup
EOF

        print_success "Backup metadata saved"
        return 0
    else
        print_error "Backup failed"
        return 1
    fi
}

# Function to restore database from backup
restore_database() {
    local backup_file=$1

    if [ -z "$backup_file" ]; then
        print_error "No backup file specified"
        list_backups
        return 1
    fi

    # Check if file exists
    if [ ! -f "$backup_file" ]; then
        print_error "Backup file not found: $backup_file"
        return 1
    fi

    print_warning "This will OVERWRITE the current database: $DB_NAME"
    read -p "Are you sure you want to continue? (yes/no): " confirm

    if [ "$confirm" != "yes" ]; then
        print_status "Restore cancelled"
        return 0
    fi

    # Create a safety backup before restore
    print_status "Creating safety backup before restore..."
    backup_database "pre_restore_$(date +%Y%m%d_%H%M%S)"

    print_status "Restoring database from: $backup_file"

    # Decompress if needed
    if [[ "$backup_file" == *.gz ]]; then
        print_status "Decompressing backup..."
        gunzip -c "$backup_file" | psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME"
    else
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" < "$backup_file"
    fi

    if [ $? -eq 0 ]; then
        print_success "Database restored successfully"
        return 0
    else
        print_error "Database restore failed"
        return 1
    fi
}

# Function to list available backups
list_backups() {
    print_status "Available backups in $BACKUP_DIR:"
    echo ""

    if [ ! -d "$BACKUP_DIR" ] || [ -z "$(ls -A $BACKUP_DIR/*.gz 2>/dev/null)" ]; then
        print_warning "No backups found"
        return 0
    fi

    printf "%-40s %-20s %-10s\n" "Backup Name" "Date" "Size"
    printf "%-40s %-20s %-10s\n" "----------------------------------------" "--------------------" "----------"

    for backup in "$BACKUP_DIR"/*.sql.gz; do
        if [ -f "$backup" ]; then
            local filename=$(basename "$backup" .sql.gz)
            local date=$(stat -c %y "$backup" | cut -d' ' -f1,2 | cut -d'.' -f1)
            local size=$(du -h "$backup" | cut -f1)
            printf "%-40s %-20s %-10s\n" "$filename" "$date" "$size"
        fi
    done
    echo ""
}

# Function to clean old backups
cleanup_old_backups() {
    local retention_days=${1:-30}

    print_status "Cleaning up backups older than $retention_days days..."

    find "$BACKUP_DIR" -name "*.sql.gz" -type f -mtime +$retention_days -delete
    find "$BACKUP_DIR" -name "*.meta" -type f -mtime +$retention_days -delete

    print_success "Old backups cleaned up"
}

# Function to verify backup integrity
verify_backup() {
    local backup_file=$1

    if [ -z "$backup_file" ]; then
        print_error "No backup file specified"
        return 1
    fi

    if [ ! -f "$backup_file" ]; then
        print_error "Backup file not found: $backup_file"
        return 1
    fi

    print_status "Verifying backup integrity: $backup_file"

    # Check if file is a valid gzip file
    if [[ "$backup_file" == *.gz ]]; then
        if gzip -t "$backup_file" 2>&1; then
            print_success "Backup file is valid"

            # Try to count SQL statements
            local stmt_count=$(gunzip -c "$backup_file" | grep -c "^INSERT\|^CREATE\|^ALTER" || true)
            print_success "Found $stmt_count SQL statements"

            return 0
        else
            print_error "Backup file is corrupted"
            return 1
        fi
    else
        print_warning "Backup file is not compressed"
        # Basic SQL file validation
        if head -n 1 "$backup_file" | grep -q "PostgreSQL"; then
            print_success "Backup file appears valid"
            return 0
        else
            print_error "Backup file may be corrupted"
            return 1
        fi
    fi
}

# Function to show database statistics
show_database_stats() {
    print_status "Database Statistics for $DB_NAME"
    echo ""

    # Table count
    local table_count=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c \
        "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" | tr -d ' ')
    echo "Tables: $table_count"

    # Database size
    local db_size=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c \
        "SELECT pg_size_pretty(pg_database_size('$DB_NAME'));" | tr -d ' ')
    echo "Database Size: $db_size"

    # Top 5 largest tables
    echo ""
    echo "Top 5 Largest Tables:"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c \
        "SELECT schemaname || '.' || tablename AS table_name,
                pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
         FROM pg_tables
         WHERE schemaname = 'public'
         ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC
         LIMIT 5;"

    echo ""
}

# Main menu
show_menu() {
    echo "=========================================="
    echo "  Database Backup & Restore Tool"
    echo "=========================================="
    echo "1. Create backup"
    echo "2. Restore from backup"
    echo "3. List backups"
    echo "4. Verify backup"
    echo "5. Cleanup old backups"
    echo "6. Show database statistics"
    echo "7. Exit"
    echo "=========================================="
}

# Main script
if [ $# -eq 0 ]; then
    # Interactive mode
    while true; do
        show_menu
        read -p "Select option: " option

        case $option in
            1)
                read -p "Enter backup name (or press Enter for auto-generated): " backup_name
                if [ -z "$backup_name" ]; then
                    backup_database
                else
                    backup_database "$backup_name"
                fi
                ;;
            2)
                list_backups
                read -p "Enter full path to backup file: " backup_file
                restore_database "$backup_file"
                ;;
            3)
                list_backups
                ;;
            4)
                read -p "Enter full path to backup file: " backup_file
                verify_backup "$backup_file"
                ;;
            5)
                read -p "Enter retention days (default 30): " retention
                cleanup_old_backups "${retention:-30}"
                ;;
            6)
                show_database_stats
                ;;
            7)
                print_status "Exiting..."
                exit 0
                ;;
            *)
                print_error "Invalid option"
                ;;
        esac
        echo ""
        read -p "Press Enter to continue..."
    done
else
    # Command-line mode
    case "$1" in
        backup)
            backup_database "$2"
            ;;
        restore)
            restore_database "$2"
            ;;
        list)
            list_backups
            ;;
        verify)
            verify_backup "$2"
            ;;
        cleanup)
            cleanup_old_backups "$2"
            ;;
        stats)
            show_database_stats
            ;;
        *)
            echo "Usage: $0 {backup|restore|list|verify|cleanup|stats} [options]"
            echo ""
            echo "Examples:"
            echo "  $0 backup                    # Create auto-named backup"
            echo "  $0 backup my_backup          # Create named backup"
            echo "  $0 restore /path/to/backup   # Restore from backup"
            echo "  $0 list                      # List all backups"
            echo "  $0 verify /path/to/backup    # Verify backup integrity"
            echo "  $0 cleanup 30                # Clean backups older than 30 days"
            echo "  $0 stats                     # Show database statistics"
            exit 1
            ;;
    esac
fi
