# VS Code Configuration for Modern Reservation

This directory contains VS Code workspace configuration optimized for the Modern Reservation project.

## 📁 Files Overview

### `settings.json`
Workspace settings that configure:
- **Biome.js** as the default formatter for TypeScript, JavaScript, and JSON files
- **Format on save** enabled
- **Auto-organize imports** on save
- Search exclusions for build artifacts

### `extensions.json`
Recommended extensions for the project:
- **biomejs.biome** - Fast formatter and linter (replaces ESLint + Prettier)
- **apollographql.vscode-apollo** - GraphQL support
- **bradlc.vscode-tailwindcss** - Tailwind CSS IntelliSense
- **vscjava.vscode-java-pack** - Java development tools
- **ms-azuretools.vscode-docker** - Docker support

### `tasks.json`
Quick tasks accessible via `Ctrl+Shift+B` (or `Cmd+Shift+B` on Mac):

#### 🚀 Service Management
- **Start All Services** - Run `./dev.sh start`
- **Stop All Services** - Run `./dev.sh stop`
- **Check Status** - Run `./dev.sh status`
- **Clean Restart** - Run `./dev.sh clean`

#### ✨ Code Quality
- **Biome Check All** - Check all Node.js projects
- **Biome Fix All** - Auto-fix all issues
- **Biome Check (Current Directory)** - Check current project only
- **Biome Fix (Current Directory)** - Fix current project only

#### 🐳 Infrastructure
- **Start Docker Infrastructure** - Start PostgreSQL, Redis, Kafka
- **Check Infrastructure** - Verify infrastructure health
- **Check Business Services** - Verify business services health

#### 💾 Database
- **Setup Database** - Initialize database schema
- **Backup Database** - Create database backup

### `modern-reservation.code-workspace`
Multi-root workspace file with launch configurations for debugging:
- Java services (Reservation, Payment, etc.)
- Node.js services (API Gateway, Notification, WebSocket)
- Next.js frontend (Guest Portal)

## 🚀 Getting Started

### 1. Install Recommended Extensions
When you open the workspace, VS Code will prompt you to install recommended extensions.

Or manually install:
```bash
code --install-extension biomejs.biome
code --install-extension apollographql.vscode-apollo
code --install-extension bradlc.vscode-tailwindcss
```

### 2. Run Tasks
Press `Ctrl+Shift+P` (or `Cmd+Shift+P`) and type "Tasks: Run Task" to see all available tasks.

Or use keyboard shortcut:
- `Ctrl+Shift+B` (or `Cmd+Shift+B`) - Shows task list

### 3. Quick Actions

#### Start Development
```
Ctrl+Shift+P → Tasks: Run Task → 🚀 Start All Services
```

#### Format Code
- **Auto**: Save file (Ctrl+S) - formats automatically
- **Manual**: Right-click → Format Document (Shift+Alt+F)
- **Fix All**: Run task "🔧 Biome Fix All"

#### Check Code Quality
```
Ctrl+Shift+P → Tasks: Run Task → ✨ Biome Check All
```

## 🎯 Project Guidelines

The configuration enforces these project rules (from `copilot-instructions.md`):

### ✅ Always Use:
1. **Shared schemas** from `@modern-reservation/schemas`
2. **Shared GraphQL client** from `@modern-reservation/graphql-client`
3. **UPPERCASE enums** (e.g., `'PENDING'`, `'CONFIRMED'`)
4. **`file:` protocol** for monorepo dependencies
5. **`./dev.sh` script** for service operations

### 🚫 Never Do:
1. Create local schema definitions
2. Create separate Apollo clients
3. Use lowercase enum values
4. Use `npm link` for monorepo packages
5. Run services directly with `docker-compose` or `mvn`

## 📝 Biome.js Configuration

All Node.js projects use Biome.js (v2.2.5) with these settings:
- **Indent**: 2 spaces
- **Line Width**: 100 characters
- **Quotes**: Single quotes
- **Semicolons**: Always
- **Trailing Commas**: ES5 style
- **Organize Imports**: Automatic

## 🔧 Customization

### Add New Task
Edit `.vscode/tasks.json`:
```json
{
  "label": "🎯 My Custom Task",
  "type": "shell",
  "command": "./my-script.sh",
  "problemMatcher": []
}
```

### Change Formatter Settings
Edit `.vscode/settings.json`:
```json
{
  "editor.formatOnSave": false,  // Disable auto-format
  "editor.defaultFormatter": null  // Use different formatter
}
```

### Add Extension
Edit `.vscode/extensions.json`:
```json
{
  "recommendations": [
    "existing.extension",
    "new.extension"
  ]
}
```

## 🐛 Troubleshooting

### Biome Not Working?
1. Install Biome extension: `code --install-extension biomejs.biome`
2. Reload VS Code: `Ctrl+Shift+P` → "Developer: Reload Window"
3. Check output: View → Output → Select "Biome"

### Tasks Not Running?
1. Ensure you're in the workspace root: `/home/navin/modern-reservation`
2. Make scripts executable: `chmod +x scripts/*.sh`
3. Check script exists: `ls -la scripts/`

### Java Debugging Not Working?
1. Install Java extension pack: `code --install-extension vscjava.vscode-java-pack`
2. Ensure Java 21 is installed: `java -version`
3. Build the project first: `./dev.sh build`

## 📚 Resources

- [Biome.js Documentation](https://biomejs.dev/)
- [VS Code Tasks](https://code.visualstudio.com/docs/editor/tasks)
- [Project Documentation](../docs/README.md)
- [Development Guide](../docs/guides/DEV_QUICK_REFERENCE.md)

## 🎓 Tips

1. **Keyboard Shortcuts**:
   - `Ctrl+Shift+P` - Command Palette
   - `Ctrl+Shift+B` - Run Build Task
   - `Shift+Alt+F` - Format Document
   - `F5` - Start Debugging

2. **Multi-Root Workspace**:
   - Open `modern-reservation.code-workspace` for best experience
   - Each project root will have its own terminal, search scope, etc.

3. **Terminal Integration**:
   - All tasks run in integrated terminal
   - View output in real-time
   - Stop tasks with `Ctrl+C`

4. **Code Actions**:
   - Hover over squiggly lines for quick fixes
   - `Ctrl+.` - Show available code actions
   - Imports organize automatically on save
