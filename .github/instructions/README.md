# Copilot Instructions for Modern Reservation

This folder contains instruction files that GitHub Copilot will reference when working on the Modern Reservation project.

## 📁 Structure

```
instructions/
├── README.md                    # This file
└── 00-core-principles.md        # Core architectural principles and patterns
```

## 🎯 Purpose

These instruction files help GitHub Copilot:
1. Understand project-specific conventions
2. Suggest appropriate tools and commands
3. Follow established architectural patterns
4. Avoid common anti-patterns
5. Maintain code consistency

## 🔗 Related Files

- `../copilot-instructions.md` - Main Copilot instructions (referenced by AI)
- `../copilot-toolsets.json` - Toolset definitions for Copilot
- `../../.vscode/` - VS Code workspace configuration
- `../../docs/` - Project documentation

## 📝 How to Use

### For Developers
1. Read through instruction files to understand project conventions
2. Follow the patterns and anti-patterns listed
3. Update instructions when new patterns emerge

### For GitHub Copilot
These files are automatically referenced by Copilot when:
- Working in the workspace
- Generating code suggestions
- Providing command recommendations
- Answering questions about the project

## ✏️ Adding New Instructions

1. Create a new markdown file with a numeric prefix:
   ```
   01-graphql-patterns.md
   02-database-conventions.md
   03-testing-guidelines.md
   ```

2. Include clear sections:
   - **Purpose**: What problem does this solve?
   - **Examples**: Show correct and incorrect patterns
   - **Rationale**: Why do we follow this convention?

3. Reference from `copilot-instructions.md` if needed

## 🎓 Instruction Categories

### Current Instructions
- **00-core-principles.md**: Fundamental project principles and anti-patterns

### Planned Instructions
- GraphQL schema patterns
- Database migration guidelines
- Testing strategies
- API design conventions
- Multi-tenancy patterns
- Event-driven architecture

## 🔄 Maintenance

- Review instructions quarterly
- Update when major architectural changes occur
- Keep examples current with latest tech stack
- Remove outdated patterns

## 📚 Additional Resources

- [Main Copilot Instructions](../copilot-instructions.md)
- [Project Documentation](../../docs/README.md)
- [Development Guide](../../docs/guides/DEV_QUICK_REFERENCE.md)
