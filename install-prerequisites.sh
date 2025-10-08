#!/bin/bash
# filepath: scripts/install-prerequisites.sh

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }

# Track installation failures
FAILED_INSTALLATIONS=()

# Function to handle command failures and retry with sudo
execute_command() {
    local cmd="$1"
    local description="$2"
    local use_sudo="${3:-false}"

    if [ "$use_sudo" = "true" ]; then
        if ! sudo bash -c "$cmd"; then
            print_error "Failed: $description"
            return 1
        fi
    else
        if ! bash -c "$cmd"; then
            print_warning "Command failed without sudo, retrying with sudo..."
            if ! sudo bash -c "$cmd"; then
                print_error "Failed even with sudo: $description"
                return 1
            fi
        fi
    fi
    return 0
}

# Function to install package with retry
install_package() {
    local package="$1"
    local description="${2:-$package}"

    print_status "Installing $description..."

    # First attempt
    if sudo apt install -y "$package" 2>/dev/null; then
        print_success "$description installed successfully"
        return 0
    fi

    # If failed, try to fix broken packages
    print_warning "Initial installation failed, attempting to fix..."
    sudo apt --fix-broken install -y 2>/dev/null
    sudo dpkg --configure -a 2>/dev/null

    # Second attempt
    if sudo apt install -y "$package" 2>/dev/null; then
        print_success "$description installed successfully (after fix)"
        return 0
    fi

    # If still failing, try update and retry
    print_warning "Still failing, updating package list and retrying..."
    sudo apt update

    # Final attempt
    if sudo apt install -y "$package" 2>/dev/null; then
        print_success "$description installed successfully (after update)"
        return 0
    fi

    print_error "Failed to install $description"
    FAILED_INSTALLATIONS+=("$description")
    return 1
}

# Check if running with sudo
if [ "$EUID" -eq 0 ]; then
   print_error "Please don't run this script as root/sudo. It will ask for sudo when needed."
   exit 1
fi

echo -e "${CYAN}════════════════════════════════════════════════════════════════${NC}"
echo -e "${CYAN}     Modern Reservation - Prerequisites Installation Script      ${NC}"
echo -e "${CYAN}════════════════════════════════════════════════════════════════${NC}"
echo ""

# Request sudo access upfront
print_status "This script requires sudo access for package installation."
sudo -v

# Keep sudo alive during script execution
while true; do sudo -n true; sleep 60; kill -0 "$$" || exit; done 2>/dev/null &

# Fix any broken packages first
print_status "Checking and fixing any broken packages..."
sudo apt --fix-broken install -y 2>/dev/null
sudo dpkg --configure -a 2>/dev/null

# Update package list
print_status "Updating package list..."
if ! sudo apt update; then
    print_warning "Package update had some issues, continuing anyway..."
fi

# Install basic utilities
print_status "Installing basic utilities..."
BASIC_PACKAGES=(curl wget git build-essential software-properties-common apt-transport-https ca-certificates gnupg lsb-release)
for package in "${BASIC_PACKAGES[@]}"; do
    install_package "$package"
done

# 1. Install Java 21
print_status "Checking Java installation..."
if ! java -version 2>&1 | grep -q "21"; then
    print_status "Installing Java 21..."
    if ! install_package "openjdk-21-jdk" "Java 21"; then
        # Try alternative Java installation
        print_warning "Trying alternative Java installation method..."
        sudo add-apt-repository -y ppa:openjdk-r/ppa 2>/dev/null
        sudo apt update
        install_package "openjdk-21-jdk" "Java 21 (alternative)"
    fi
else
    print_success "Java 21 already installed"
fi

# Set JAVA_HOME
if ! grep -q "JAVA_HOME" ~/.bashrc; then
    JAVA_PATH=$(readlink -f $(which java) 2>/dev/null | sed "s:/bin/java::")
    if [ -n "$JAVA_PATH" ]; then
        echo "export JAVA_HOME=$JAVA_PATH" >> ~/.bashrc
        echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.bashrc
        print_success "JAVA_HOME configured"
    else
        print_warning "Could not auto-detect JAVA_HOME, please set manually"
    fi
fi

# 2. Install Maven
print_status "Checking Maven installation..."
if ! command -v mvn &> /dev/null; then
    install_package "maven" "Maven"
else
    print_success "Maven already installed"
fi

# 3. Install Node.js 20 LTS
print_status "Checking Node.js installation..."
if ! node --version 2>&1 | grep -q "v20"; then
    print_status "Installing Node.js 20 LTS..."

    # Remove old NodeSource repository if exists
    sudo rm -f /etc/apt/sources.list.d/nodesource.list
    sudo rm -f /usr/share/keyrings/nodesource.gpg

    # Install Node.js 20
    if ! curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -; then
        print_warning "NodeSource setup failed, trying alternative method..."
        # Alternative: Install from snap
        sudo snap install node --classic --channel=20
    else
        install_package "nodejs" "Node.js 20"
    fi
else
    print_success "Node.js 20 already installed"
fi

# 4. Install npm and pnpm
print_status "Installing/updating npm and pnpm..."
if command -v npm &> /dev/null; then
    execute_command "npm install -g npm@latest" "npm update" true
    execute_command "npm install -g pnpm" "pnpm installation" true
else
    print_error "npm not found, skipping pnpm installation"
    FAILED_INSTALLATIONS+=("pnpm")
fi

# 5. Install Docker
print_status "Checking Docker installation..."
if ! command -v docker &> /dev/null; then
    print_status "Installing Docker..."

    # Remove old versions
    sudo apt remove -y docker docker-engine docker.io containerd runc 2>/dev/null || true

    # Add Docker's official GPG key
    sudo mkdir -m 0755 -p /etc/apt/keyrings

    # Download and add GPG key with retry
    MAX_RETRIES=3
    RETRY_COUNT=0
    while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
        if curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg; then
            break
        fi
        RETRY_COUNT=$((RETRY_COUNT + 1))
        print_warning "Failed to download Docker GPG key, retry $RETRY_COUNT/$MAX_RETRIES"
        sleep 2
    done

    if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
        print_error "Failed to download Docker GPG key"
        FAILED_INSTALLATIONS+=("Docker")
    else
        # Set up the repository
        echo \
          "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
          $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

        # Update and install Docker
        sudo apt update

        if install_package "docker-ce" "Docker CE" && \
           install_package "docker-ce-cli" "Docker CLI" && \
           install_package "containerd.io" "Containerd" && \
           install_package "docker-buildx-plugin" "Docker Buildx" && \
           install_package "docker-compose-plugin" "Docker Compose Plugin"; then

            # Add user to docker group
            sudo usermod -aG docker $USER

            # Start Docker service
            sudo systemctl enable docker 2>/dev/null || true
            sudo systemctl start docker 2>/dev/null || true

            print_success "Docker installed successfully"
            print_warning "You need to log out and back in for docker group changes to take effect"
        else
            print_error "Some Docker components failed to install"
            FAILED_INSTALLATIONS+=("Docker components")
        fi
    fi
else
    print_success "Docker already installed"
fi

# 6. Install Docker Compose standalone (v2)
print_status "Checking Docker Compose v2..."
if ! docker compose version &> /dev/null 2>&1; then
    install_package "docker-compose-plugin" "Docker Compose v2"
else
    print_success "Docker Compose already installed"
fi

# 7. Install PostgreSQL client
install_package "postgresql-client" "PostgreSQL client"

# 8. Install Redis client
install_package "redis-tools" "Redis client"

# 9. Install additional tools
print_status "Installing additional development tools..."
ADDITIONAL_TOOLS=(ripgrep fd-find bat jq tree htop make unzip zip net-tools lsof telnet vim nano)
for tool in "${ADDITIONAL_TOOLS[@]}"; do
    install_package "$tool"
done

# 10. Install GitHub CLI
print_status "Checking GitHub CLI installation..."
if ! command -v gh &> /dev/null; then
    print_status "Installing GitHub CLI..."

    # Add GitHub CLI repository
    MAX_RETRIES=3
    RETRY_COUNT=0
    while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
        if curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg 2>/dev/null; then
            break
        fi
        RETRY_COUNT=$((RETRY_COUNT + 1))
        print_warning "Failed to download GitHub CLI GPG key, retry $RETRY_COUNT/$MAX_RETRIES"
        sleep 2
    done

    if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
        print_error "Failed to download GitHub CLI GPG key"
        FAILED_INSTALLATIONS+=("GitHub CLI")
    else
        sudo chmod go+r /usr/share/keyrings/githubcli-archive-keyring.gpg
        echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null

        # Update and install
        sudo apt update
        if install_package "gh" "GitHub CLI"; then
            print_success "GitHub CLI installed successfully"
            print_warning "Run 'gh auth login' to authenticate with GitHub"
        fi
    fi
else
    print_success "GitHub CLI already installed"
fi

# 11. Install global npm packages
if command -v npm &> /dev/null; then
    print_status "Installing global npm packages..."

    # Install Nx CLI
    execute_command "npm install -g nx@latest" "Nx CLI installation" true

    # Install Angular CLI
    execute_command "npm install -g @angular/cli@latest" "Angular CLI installation" true

    # Install TypeScript
    execute_command "npm install -g typescript" "TypeScript installation" true
else
    print_warning "npm not available, skipping global package installations"
    FAILED_INSTALLATIONS+=("Global npm packages")
fi

# Verify installations
echo ""
echo -e "${CYAN}════════════════════════════════════════════════════════════════${NC}"
echo -e "${CYAN}                    Installation Summary                          ${NC}"
echo -e "${CYAN}════════════════════════════════════════════════════════════════${NC}"

# Check Java
if java -version &> /dev/null 2>&1; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    print_success "✅ Java: $JAVA_VERSION"
else
    print_error "❌ Java not found"
fi

# Check Maven
if mvn -version &> /dev/null 2>&1; then
    MVN_VERSION=$(mvn -version 2>&1 | head -n 1)
    print_success "✅ Maven: $MVN_VERSION"
else
    print_error "❌ Maven not found"
fi

# Check Node.js
if node --version &> /dev/null 2>&1; then
    NODE_VERSION=$(node --version)
    print_success "✅ Node.js: $NODE_VERSION"
else
    print_error "❌ Node.js not found"
fi

# Check npm
if npm --version &> /dev/null 2>&1; then
    NPM_VERSION=$(npm --version)
    print_success "✅ npm: $NPM_VERSION"
else
    print_error "❌ npm not found"
fi

# Check pnpm
if pnpm --version &> /dev/null 2>&1; then
    PNPM_VERSION=$(pnpm --version)
    print_success "✅ pnpm: $PNPM_VERSION"
else
    print_error "❌ pnpm not found"
fi

# Check Docker
if docker --version &> /dev/null 2>&1; then
    DOCKER_VERSION=$(docker --version)
    print_success "✅ Docker: $DOCKER_VERSION"
else
    print_error "❌ Docker not found"
fi

# Check Docker Compose
if docker compose version &> /dev/null 2>&1; then
    COMPOSE_VERSION=$(docker compose version 2>&1)
    print_success "✅ Docker Compose: $COMPOSE_VERSION"
else
    print_error "❌ Docker Compose not found"
fi

# Check PostgreSQL client
if psql --version &> /dev/null 2>&1; then
    PSQL_VERSION=$(psql --version)
    print_success "✅ PostgreSQL client: $PSQL_VERSION"
else
    print_error "❌ PostgreSQL client not found"
fi

# Check Redis client
if redis-cli --version &> /dev/null 2>&1; then
    REDIS_VERSION=$(redis-cli --version)
    print_success "✅ Redis client: $REDIS_VERSION"
else
    print_error "❌ Redis client not found"
fi

# Check Nx
if nx --version &> /dev/null 2>&1; then
    NX_VERSION=$(nx --version 2>&1)
    print_success "✅ Nx CLI: $NX_VERSION"
else
    print_error "❌ Nx CLI not found"
fi

# Check ripgrep
if rg --version &> /dev/null 2>&1; then
    RG_VERSION=$(rg --version | head -n 1)
    print_success "✅ Ripgrep: $RG_VERSION"
else
    print_error "❌ Ripgrep not found"
fi

# Check fd-find
if fd --version &> /dev/null 2>&1 || fdfind --version &> /dev/null 2>&1; then
    if fd --version &> /dev/null 2>&1; then
        FD_VERSION=$(fd --version)
    else
        FD_VERSION=$(fdfind --version)
    fi
    print_success "✅ fd-find: $FD_VERSION"
else
    print_error "❌ fd-find not found"
fi

# Check bat
if bat --version &> /dev/null 2>&1 || batcat --version &> /dev/null 2>&1; then
    if bat --version &> /dev/null 2>&1; then
        BAT_VERSION=$(bat --version | head -n 1)
    else
        BAT_VERSION=$(batcat --version | head -n 1)
    fi
    print_success "✅ bat: $BAT_VERSION"
else
    print_error "❌ bat not found"
fi

# Check GitHub CLI
if gh --version &> /dev/null 2>&1; then
    GH_VERSION=$(gh --version | head -n 1)
    print_success "✅ GitHub CLI: $GH_VERSION"
else
    print_error "❌ GitHub CLI not found"
fi

# Report failed installations
if [ ${#FAILED_INSTALLATIONS[@]} -gt 0 ]; then
    echo ""
    echo -e "${RED}════════════════════════════════════════════════════════════════${NC}"
    echo -e "${RED}                    Failed Installations                         ${NC}"
    echo -e "${RED}════════════════════════════════════════════════════════════════${NC}"
    for item in "${FAILED_INSTALLATIONS[@]}"; do
        echo -e "${RED}❌ $item${NC}"
    done
    echo ""
    print_warning "Some installations failed. You may need to install them manually."
fi

echo ""
echo -e "${CYAN}════════════════════════════════════════════════════════════════${NC}"
if [ ${#FAILED_INSTALLATIONS[@]} -eq 0 ]; then
    echo -e "${GREEN}Installation Complete - All components installed successfully!${NC}"
else
    echo -e "${YELLOW}Installation Complete - Some components need manual installation${NC}"
fi
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo "1. Log out and log back in (for Docker group changes)"
echo "2. Source your bashrc: source ~/.bashrc"
echo "3. Authenticate with GitHub: gh auth login"
echo "4. Run the dependency check: ./scripts/check-dependencies.sh"
echo "5. Start the application: ./dev.sh docker-start"
echo ""
echo -e "${CYAN}════════════════════════════════════════════════════════════════${NC}"

# Clean up background sudo refresh
kill %1 2>/dev/null
