# 📝 Optional: Add Documentation Badge to README

Once GitHub Pages is enabled, you can add a professional documentation badge to your main README.md.

## Badge Options

### Option 1: Simple Documentation Badge
```markdown
[![Documentation](https://img.shields.io/badge/docs-GitHub%20Pages-blue.svg)](https://red2n.github.io/modern-reservation/)
```

**Preview:**
[![Documentation](https://img.shields.io/badge/docs-GitHub%20Pages-blue.svg)](https://red2n.github.io/modern-reservation/)

### Option 2: With Icon
```markdown
[![📚 Documentation](https://img.shields.io/badge/📚-Documentation-blue.svg)](https://red2n.github.io/modern-reservation/)
```

**Preview:**
[![📚 Documentation](https://img.shields.io/badge/📚-Documentation-blue.svg)](https://red2n.github.io/modern-reservation/)

### Option 3: Styled Badge
```markdown
[![Documentation](https://img.shields.io/badge/Documentation-Available-success?style=for-the-badge)](https://red2n.github.io/modern-reservation/)
```

**Preview:**
[![Documentation](https://img.shields.io/badge/Documentation-Available-success?style=for-the-badge)](https://red2n.github.io/modern-reservation/)

### Option 4: Status Badge (After enabling)
```markdown
[![GitHub Pages](https://img.shields.io/github/deployments/red2n/modern-reservation/github-pages?label=docs)](https://red2n.github.io/modern-reservation/)
```

This will show deployment status automatically!

## Where to Add

Add the badge near the top of your `README.md`, with other badges:

```markdown
# Modern Reservation Management System 🏨

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Documentation](https://img.shields.io/badge/docs-GitHub%20Pages-blue.svg)](https://red2n.github.io/modern-reservation/)
[![TypeScript](https://img.shields.io/badge/%3C%2F%3E-TypeScript-%230074c1.svg)](https://www.typescriptlang.org/)
...
```

## Quick Links Section

You can also add a dedicated documentation section:

```markdown
## 📚 Documentation

- **[📖 Full Documentation](https://red2n.github.io/modern-reservation/)** - Complete docs on GitHub Pages
- **[🚀 Quick Start](docs/guides/DEV_QUICK_REFERENCE.md)** - Get started quickly
- **[🏗️ Architecture](docs/architecture/)** - System design
- **[📊 API Reference](docs/references/)** - Technical references
```

## Social Sharing

When sharing your project, use the GitHub Pages URL:
```
Check out the documentation: https://red2n.github.io/modern-reservation/
```

GitHub will automatically generate a nice preview card with your site title and description from `_config.yml`!
