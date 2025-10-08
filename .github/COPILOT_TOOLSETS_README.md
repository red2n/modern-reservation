# 🎯 GitHub Copilot Toolsets - Complete Setup Guide

## 📍 Quick Start

This guide shows you how to configure GitHub Copilot to use project-specific commands and tools.

## 🎬 What You Need to Do

### Step 1: Copy User Toolsets File

**Windows Path (Your System):**
```
C:\Users\subramanin\AppData\Roaming\Code\User\prompts\copilot-tool.toolsets.jsonc
```

**What to Do:**
1. Open Windows Explorer
2. Navigate to: `C:\Users\subramanin\AppData\Roaming\Code\User\`
3. Create folder named `prompts` if it doesn't exist
4. Copy the file from `.github/copilot-tool.toolsets.jsonc` to this location
5. Rename it exactly: `copilot-tool.toolsets.jsonc`

**Quick Command (PowerShell):**
```powershell
# Create directory
New-Item -ItemType Directory -Force -Path "$env:APPDATA\Code\User\prompts"

# Copy from WSL to Windows (if using WSL)
cp /home/navin/modern-reservation/.github/copilot-tool.toolsets.jsonc /mnt/c/Users/subramanin/AppData/Roaming/Code/User/prompts/copilot-tool.toolsets.jsonc
```

**Alternative: Manual Copy**
```powershell
# In PowerShell
notepad "$env:APPDATA\Code\User\prompts\copilot-tool.toolsets.jsonc"
# Then paste content from .github/copilot-tool.toolsets.jsonc
```

### Step 2: Restart VS Code
1. Close VS Code completely
2. Reopen the Modern Reservation workspace
3. GitHub Copilot will now use the toolsets

### Step 3: Test It Works

Open GitHub Copilot Chat and ask:
```
You: "How should I search for files in this project?"
Expected: Copilot suggests using 'rg' (ripgrep) instead of 'grep'

You: "How do I start the services?"
Expected: Copilot suggests './dev.sh start' not 'docker-compose up'

You: "How do I format my code?"
Expected: Copilot suggests 'npx biome check' not 'prettier' or 'eslint'
```

## 📁 What We Created

### 1. Project Files (Already Done ✅)

```
.github/
├── copilot-instructions.md           # Main instructions for Copilot
├── copilot-toolsets.json              # Project-specific toolsets
├── copilot-tool.toolsets.jsonc        # Template for user config
└── instructions/
    ├── README.md                      # Instructions overview
    └── 00-core-principles.md          # Core architectural principles
```

### 2. User File (You Need to Create)

```
C:\Users\subramanin\AppData\Roaming\Code\User\prompts\
└── copilot-tool.toolsets.jsonc        # Your personal config
```

## 🎯 What Copilot Will Now Know

### ✅ Preferred Commands

| Instead of... | Use... |
|--------------|--------|
| `grep` | `rg` (ripgrep) |
| `eslint` / `prettier` | `npx biome check` |
| `docker-compose up` | `./dev.sh start` |
| `npm link` | `file:` protocol |
| Creating local schemas | Import from `@modern-reservation/schemas` |

### ✅ Project Conventions

- **Enums**: UPPERCASE (`'PENDING'`, `'CONFIRMED'`)
- **Quotes**: Single quotes (`'`)
- **Indentation**: 2 spaces
- **Line width**: 100 characters
- **Semicolons**: Always required
- **Trailing commas**: ES5 style

### ✅ Architecture Rules

- Use shared libraries (schemas, graphql-client, ui-components)
- Follow SOLID principles
- Use `./dev.sh` for all operations
- Never duplicate code between apps

## 🔍 Troubleshooting

### Copilot Not Following Instructions?

1. **Check file location is correct**
   ```powershell
   # Should show the file
   dir "$env:APPDATA\Code\User\prompts\copilot-tool.toolsets.jsonc"
   ```

2. **Restart VS Code completely**
   - Don't just reload window
   - Close all VS Code windows
   - Reopen from start

3. **Check Copilot is active**
   - Bottom right: Look for Copilot icon
   - Should show "Copilot: Active"

4. **Verify JSON syntax**
   - Open the file in VS Code
   - Check for red squiggly lines
   - Fix any JSON errors

### File Path Issues

**WSL Users:**
If using WSL, you have two options:

**Option A: Windows Path (Recommended)**
```
C:\Users\subramanin\AppData\Roaming\Code\User\prompts\copilot-tool.toolsets.jsonc
```

**Option B: WSL Path**
```
/home/navin/.config/Code/User/prompts/copilot-tool.toolsets.jsonc
```

Both work, but Windows path is preferred when VS Code is installed on Windows.

## 📚 Toolsets Available

### 1. **modern-reservation**
Main project toolset with all conventions

### 2. **search-tools**
Fast file searching with ripgrep
```bash
rg "searchTerm" -t typescript
rg -i "case-insensitive"
```

### 3. **code-quality**
Code formatting with Biome.js
```bash
npx biome check .
npx biome check --write .
```

### 4. **service-management**
Service operations
```bash
./dev.sh start
./dev.sh stop
./dev.sh status
```

### 5. **database**
PostgreSQL operations
```bash
psql -h localhost -U modernreservation
./scripts/setup-database.sh
```

### 6. **monorepo**
Workspace management
```json
{
  "dependencies": {
    "@modern-reservation/schemas": "file:../../../libs/shared/schemas"
  }
}
```

### 7. **graphql**
GraphQL operations
```typescript
import { useGetPropertiesQuery } from '@modern-reservation/graphql-client';
```

### 8. **docker**
Container management
```bash
./dev.sh docker-start
docker ps
docker logs <container>
```

### 9. **git-conventions**
Git workflow
```bash
git commit -m "feat: add new feature"
git pull --rebase
```

### 10. **linux-commands**
Preferred CLI tools
```bash
rg instead of grep
bat instead of cat
tree for directory structure
```

### 11. **testing**
Testing guidelines
```typescript
// test file: component.test.ts
describe('Component', () => {
  it('should render correctly', () => {
    // test code
  });
});
```

## 🎓 Examples of Copilot Behavior

### Before Toolsets
```
You: "Search for 'useEffect' in TypeScript files"
Copilot: grep -r "useEffect" --include="*.ts"
```

### After Toolsets
```
You: "Search for 'useEffect' in TypeScript files"
Copilot: rg "useEffect" -t ts
```

---

### Before Toolsets
```
You: "Format this file"
Copilot: npx prettier --write file.ts
```

### After Toolsets
```
You: "Format this file"
Copilot: npx biome check --write file.ts
```

---

### Before Toolsets
```
You: "Start the application"
Copilot: docker-compose up
```

### After Toolsets
```
You: "Start the application"
Copilot: ./dev.sh start
```

## 📝 Customization

You can add your own toolsets to the user config file:

```jsonc
{
  "toolsets": {
    // Keep existing toolsets...

    // Add your custom toolset
    "my-custom-tools": {
      "description": "My personal preferences",
      "icon": "star",
      "instructions": [
        "Your custom instructions here",
        "More instructions"
      ]
    }
  }
}
```

## 🔗 Related Documentation

- [Copilot Instructions](.github/copilot-instructions.md)
- [Core Principles](.github/instructions/00-core-principles.md)
- [VS Code Configuration](docs/VSCODE_CONFIGURATION_COMPLETE.md)
- [Full Setup Guide](docs/COPILOT_TOOLSETS_SETUP.md)

## ✅ Checklist

- [ ] Created `prompts` folder in VS Code user directory
- [ ] Copied `copilot-tool.toolsets.jsonc` to correct location
- [ ] Restarted VS Code completely
- [ ] Tested Copilot with sample questions
- [ ] Verified Copilot suggests correct commands
- [ ] Customized with personal preferences (optional)

## 🎉 You're All Set!

Once the file is in place and VS Code is restarted, GitHub Copilot will:
- ✅ Suggest ripgrep instead of grep
- ✅ Suggest Biome.js instead of ESLint/Prettier
- ✅ Suggest ./dev.sh instead of direct commands
- ✅ Follow project conventions automatically
- ✅ Understand your architecture patterns

**Need help?** Check the [full documentation](docs/COPILOT_TOOLSETS_SETUP.md) or open an issue.
