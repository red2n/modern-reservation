# GitHub Copilot Toolsets Configuration Guide

## 📍 Overview

This guide explains how to configure GitHub Copilot to use project-specific toolsets and commands for the Modern Reservation project.

## 🎯 What Are Toolsets?

Toolsets tell GitHub Copilot:
- Which tools to prefer (e.g., `ripgrep` instead of `grep`)
- Project-specific commands (e.g., `./dev.sh start` instead of `docker-compose up`)
- Code patterns and conventions to follow
- Anti-patterns to avoid

## 📁 File Locations

### Project Toolsets (Already Created)
- **Location**: `.github/copilot-toolsets.json`
- **Purpose**: Defines toolsets for this project
- **Scope**: Only applies when working in this workspace

### User Toolsets (You Need to Create)
- **Windows Location**: `C:/Users/subramanin/AppData/Roaming/Code/User/prompts/copilot-tool.toolsets.jsonc`
- **Linux/WSL Location**: `~/.config/Code/User/prompts/copilot-tool.toolsets.jsonc`
- **Purpose**: References project toolsets and adds personal preferences
- **Scope**: Applies to all your VS Code workspaces

## 🔧 Setup Instructions

### Step 1: Create User Toolsets Directory

**On Windows:**
```powershell
mkdir "C:\Users\subramanin\AppData\Roaming\Code\User\prompts"
```

**On Linux/WSL:**
```bash
mkdir -p ~/.config/Code/User/prompts
```

### Step 2: Create Toolsets File

Create a file named `copilot-tool.toolsets.jsonc` in the prompts directory.

**File Path Examples:**
- Windows: `C:/Users/subramanin/AppData/Roaming/Code/User/prompts/copilot-tool.toolsets.jsonc`
- Linux: `~/.config/Code/User/prompts/copilot-tool.toolsets.jsonc`
- WSL: `/home/navin/.config/Code/User/prompts/copilot-tool.toolsets.jsonc`

### Step 3: Add Configuration

Copy this content to your `copilot-tool.toolsets.jsonc` file:

```jsonc
{
  "$schema": "https://aka.ms/copilot/toolsets-schema",
  "version": "1.0",
  "toolsets": {
    // Reference Modern Reservation project toolsets
    "modern-reservation": {
      "description": "Modern Reservation project-specific tools and conventions",
      "icon": "tools",
      "extends": [
        "file:///${workspaceFolder}/.github/copilot-toolsets.json"
      ],
      "tools": [
        "ripgrep",
        "biome",
        "dev.sh"
      ],
      "instructions": [
        "Read instructions from ${workspaceFolder}/.github/instructions/",
        "Always use ripgrep (rg) instead of grep",
        "Use Biome.js instead of ESLint or Prettier",
        "Use ./dev.sh for all service operations",
        "Use UPPERCASE enum values",
        "Use file: protocol for monorepo dependencies",
        "Import from shared packages: @modern-reservation/schemas, @modern-reservation/graphql-client"
      ]
    },

    // Personal preferences
    "search": {
      "description": "Fast searching with ripgrep",
      "icon": "search",
      "tools": ["ripgrep"],
      "instructions": [
        "Use 'rg' instead of 'grep' for faster file searching",
        "Use 'rg -t ts' to search TypeScript files",
        "Use 'rg -t java' to search Java files",
        "Use 'rg -i' for case-insensitive search",
        "Use 'rg -l' to list only filenames",
        "Use 'rg --context 3' to show context lines"
      ]
    },

    "format": {
      "description": "Code formatting preferences",
      "icon": "sparkle",
      "instructions": [
        "Prefer Biome.js over ESLint and Prettier",
        "Single quotes instead of double quotes",
        "2-space indentation",
        "100 character line width",
        "Always include semicolons",
        "ES5 trailing commas"
      ]
    },

    "git": {
      "description": "Git command preferences",
      "icon": "git-commit",
      "instructions": [
        "Use conventional commit messages: feat:, fix:, docs:, chore:, etc.",
        "Prefer 'git status' over long form commands",
        "Use 'git log --oneline --graph' for history",
        "Always pull before push: 'git pull --rebase'",
        "Use descriptive branch names: feature/, bugfix/, hotfix/"
      ]
    }
  }
}
```

### Step 4: Verify Setup

1. **Restart VS Code**
2. **Open Modern Reservation project**
3. **Open GitHub Copilot Chat**
4. **Test with a query**: "How should I search for TypeScript files in this project?"
   - Copilot should suggest using `rg -t ts` instead of `grep`

## 📝 Available Toolsets in Modern Reservation

The project defines these toolsets in `.github/copilot-toolsets.json`:

| Toolset | Description | Key Tools |
|---------|-------------|-----------|
| `modern-reservation-dev` | Main development toolset | ripgrep, biome, docker |
| `code-search` | Fast code searching | ripgrep |
| `code-quality` | Formatting and linting | biome |
| `service-management` | Service operations | dev.sh |
| `database-operations` | PostgreSQL operations | psql |
| `monorepo` | Workspace management | npm, nx |
| `graphql` | GraphQL operations | apollo |
| `docker` | Container management | docker, docker-compose |
| `java-spring` | Spring Boot services | maven, java |
| `nodejs-typescript` | Node.js services | node, typescript |
| `frontend-nextjs` | Next.js frontend | nextjs, tailwindcss |

## 🎯 How Copilot Uses Toolsets

### Before Toolsets
```plaintext
You: "How do I search for a string in files?"
Copilot: "Use grep -r 'searchTerm' ."
```

### After Toolsets
```plaintext
You: "How do I search for a string in files?"
Copilot: "Use ripgrep for faster searches: rg 'searchTerm' --type typescript"
```

### Example Interactions

**Starting Services:**
```plaintext
You: "How do I start the services?"
Copilot: "Use ./dev.sh start (not docker-compose up)"
```

**Code Formatting:**
```plaintext
You: "How do I format my code?"
Copilot: "Use npx biome check --write . (not prettier or eslint)"
```

**Creating Schemas:**
```plaintext
You: "I need to create a validation schema"
Copilot: "Import from @modern-reservation/schemas instead of creating local Zod schemas"
```

## 🔍 Debugging Toolsets

### Check if Toolsets are Loaded
1. Open Command Palette (`Ctrl+Shift+P`)
2. Type "GitHub Copilot: Show Toolsets"
3. Verify "modern-reservation" toolset appears

### Common Issues

#### Toolset Not Found
- **Cause**: File path incorrect
- **Fix**: Verify file exists at exact location
- **Check**: `ls ~/.config/Code/User/prompts/copilot-tool.toolsets.jsonc`

#### Toolsets Not Applied
- **Cause**: VS Code hasn't reloaded
- **Fix**: Restart VS Code completely
- **Alternative**: Run "Developer: Reload Window"

#### Syntax Errors
- **Cause**: Invalid JSON
- **Fix**: Validate JSON syntax (remove trailing commas in JSON, use JSONC for comments)
- **Tool**: Use online JSON validator

### Verify Project Toolsets
```bash
# Check project toolsets exist
cat .github/copilot-toolsets.json | head -20

# Verify instructions folder
ls -la .github/instructions/
```

## 📚 Advanced Configuration

### Extend Multiple Toolsets
```jsonc
{
  "toolsets": {
    "my-toolset": {
      "extends": [
        "file:///${workspaceFolder}/.github/copilot-toolsets.json",
        "file:///path/to/other-toolsets.json"
      ]
    }
  }
}
```

### Workspace-Specific Overrides
```jsonc
{
  "toolsets": {
    "modern-reservation-custom": {
      "extends": ["file:///${workspaceFolder}/.github/copilot-toolsets.json"],
      "instructions": [
        // Override or add custom instructions
        "Additional custom instruction for this workspace"
      ]
    }
  }
}
```

### Environment-Specific Tools
```jsonc
{
  "toolsets": {
    "linux-tools": {
      "tools": ["ripgrep", "fd", "bat"],
      "instructions": [
        "Use 'rg' for search",
        "Use 'fd' for find",
        "Use 'bat' instead of cat"
      ]
    },
    "windows-tools": {
      "tools": ["everything", "ag"],
      "instructions": [
        "Use Everything search on Windows",
        "Use ag (Silver Searcher) if ripgrep not available"
      ]
    }
  }
}
```

## 🎓 Best Practices

1. **Keep Instructions Specific**: Clear, actionable instructions work best
2. **Use Examples**: Show correct and incorrect patterns
3. **Update Regularly**: Keep toolsets current with project changes
4. **Test Thoroughly**: Verify Copilot follows instructions
5. **Document Decisions**: Explain why certain tools are preferred

## 🔗 Related Documentation

- [GitHub Copilot Toolsets Documentation](https://docs.github.com/en/copilot/customizing-copilot/creating-custom-toolsets)
- [Project Copilot Instructions](copilot-instructions.md)
- [VS Code Configuration](../docs/VSCODE_CONFIGURATION_COMPLETE.md)
- [Development Guide](../docs/guides/DEV_QUICK_REFERENCE.md)

## ❓ Need Help?

If Copilot isn't following toolset instructions:
1. Check file paths are correct
2. Restart VS Code completely
3. Verify JSON syntax is valid
4. Check Copilot output panel for errors
5. Review instruction specificity

---

**Next Steps:**
1. ✅ Create user toolsets file at correct location
2. ✅ Copy configuration from this guide
3. ✅ Restart VS Code
4. ✅ Test with Copilot Chat
5. ✅ Customize with your preferences
