# 📚 GitHub Pages Documentation Setup

## Overview

The `docs/` folder is configured to be published as a **GitHub Pages** site, providing a professional documentation website for the Modern Reservation System.

## 🌐 Live Documentation URL

Once enabled, the documentation will be available at:
```
https://red2n.github.io/modern-reservation/
```

## 📁 Documentation Structure

```
docs/
├── index.md                      # Main landing page
├── _config.yml                   # Jekyll configuration
├── README.md                     # Documentation overview
│
├── guides/                       # Development guides
│   ├── DEV_QUICK_REFERENCE.md
│   ├── CLEAN_RESTART_GUIDE.md
│   ├── SCRIPT_ORGANIZATION.md
│   └── SINGLE_ENTRY_POINT.md
│
├── architecture/                 # Architecture documentation
│   ├── event-driven-architecture-diagram.md
│   ├── KAFKA_IMPLEMENTATION_GUIDE.md
│   ├── KAFKA_QUICK_START.md
│   └── ...
│
├── references/                   # Technical references
│   ├── AVRO_QUICK_REFERENCE.md
│   └── AVRO_MIGRATION_COMPLETE.md
│
├── deployment/                   # Deployment guides
│   └── network-isolation-guide.md
│
└── [project management docs]
    ├── product-requirements-document.md
    ├── project-development-plan.md
    └── PHASE_0_COMPLETE.md
```

## ⚙️ How to Enable GitHub Pages

### 1. Navigate to Repository Settings
```
https://github.com/red2n/modern-reservation/settings/pages
```

### 2. Configure Source
- **Source**: Deploy from a branch
- **Branch**: `main` (or your default branch)
- **Folder**: `/docs`

### 3. Save Settings
Click "Save" and GitHub will start building your site.

### 4. Wait for Deployment
- Initial deployment takes 1-2 minutes
- You'll see a notification when the site is live
- Visit: `https://red2n.github.io/modern-reservation/`

## 🎨 Theme Configuration

The site uses the **Cayman** theme configured in `_config.yml`:

```yaml
theme: jekyll-theme-cayman
title: Modern Reservation System
description: Cloud-native microservices hotel reservation platform
```

### Available Themes
If you want to change the theme, edit `docs/_config.yml`:
- `jekyll-theme-cayman` (current)
- `jekyll-theme-minimal`
- `jekyll-theme-architect`
- `jekyll-theme-slate`
- `jekyll-theme-modernist`
- `jekyll-theme-tactile`
- `jekyll-theme-time-machine`
- `jekyll-theme-leap-day`
- `jekyll-theme-merlot`
- `jekyll-theme-midnight`
- `jekyll-theme-dinky`
- `jekyll-theme-hacker`

## 📝 Content Organization

### Guides (`guides/`)
Step-by-step tutorials and how-to guides:
- Daily development workflow
- Environment setup
- Script usage
- Best practices

### Architecture (`architecture/`)
System design and architecture documentation:
- Event-driven architecture
- Kafka implementation
- Service design
- Data flow diagrams

### References (`references/`)
Technical reference materials:
- Avro schema documentation
- API references
- Configuration options
- Command references

### Deployment (`deployment/`)
Production deployment guides:
- Network configuration
- Security setup
- Infrastructure as code
- Monitoring setup

## 🔗 Internal Linking

Use relative paths for internal links:

```markdown
# Link to another doc in same folder
[Clean Restart](CLEAN_RESTART_GUIDE.md)

# Link to doc in different folder
[Architecture](../architecture/event-driven-architecture-diagram.md)

# Link to root-level doc
[Main README](../README.md)
```

## 🖼️ Adding Images

Store images in an `assets/` folder:

```
docs/
└── assets/
    └── images/
        └── architecture-diagram.png
```

Reference in markdown:
```markdown
![Architecture Diagram](assets/images/architecture-diagram.png)
```

## ✅ Best Practices

### 1. **Front Matter** (Optional but Recommended)
Add YAML front matter to documents for better control:

```markdown
---
layout: default
title: Clean Restart Guide
description: How to perform a clean restart
---

# Content starts here
```

### 2. **Navigation**
- Keep navigation clear in `index.md`
- Create breadcrumbs for deep navigation
- Link back to main docs from subdirectories

### 3. **Search Engine Optimization**
- Use descriptive titles
- Add meta descriptions in front matter
- Use proper heading hierarchy (h1 → h2 → h3)

### 4. **Code Blocks**
Use syntax highlighting:

````markdown
```bash
./dev.sh start
```

```java
@Service
public class ReservationService {
    // Java code
}
```

```typescript
interface Reservation {
    id: string;
    // TypeScript code
}
```
````

### 5. **Tables**
Use markdown tables for structured data:

```markdown
| Command | Description |
|---------|-------------|
| `./dev.sh start` | Start all services |
| `./dev.sh stop` | Stop all services |
```

## 🔄 Updating Documentation

### Local Testing
You can test GitHub Pages locally:

```bash
# Install Jekyll
gem install jekyll bundler

# Navigate to docs folder
cd docs

# Serve locally
jekyll serve

# Open http://localhost:4000/modern-reservation/
```

### Deployment
Changes are automatically deployed when pushed to the main branch:

```bash
# Make changes
git add docs/
git commit -m "docs: update documentation"
git push origin main

# GitHub Actions will rebuild and deploy automatically
```

## 📊 Analytics (Optional)

Add Google Analytics to track documentation usage:

1. Get your GA tracking ID
2. Add to `_config.yml`:

```yaml
google_analytics: UA-XXXXXXXXX-X
```

## 🎯 Features Enabled

### Jekyll Plugins
The following plugins are enabled in `_config.yml`:

- **jekyll-relative-links**: Automatic link resolution
- **jekyll-optional-front-matter**: Front matter not required
- **jekyll-readme-index**: README.md as index
- **jekyll-titles-from-headings**: Auto-extract titles
- **jekyll-github-metadata**: GitHub repo metadata

### Markdown Features
- **GitHub Flavored Markdown (GFM)**
- **Syntax highlighting** (Rouge)
- **Emoji support** 🎉
- **Task lists** support
- **Tables** support
- **Footnotes** support

## 🚀 Quick Links

After enabling GitHub Pages, your documentation will be available at:

- **Home**: https://red2n.github.io/modern-reservation/
- **Guides**: https://red2n.github.io/modern-reservation/guides/
- **Architecture**: https://red2n.github.io/modern-reservation/architecture/
- **References**: https://red2n.github.io/modern-reservation/references/

## 📞 Support

If you encounter issues with GitHub Pages:

1. Check the **Pages** tab in repository settings
2. View **Actions** tab for build logs
3. Verify `_config.yml` syntax
4. Check that all links use relative paths
5. Ensure markdown files have `.md` extension

## 📝 Summary

✅ **Complete**: Documentation structure organized
✅ **Ready**: Jekyll configuration created
✅ **Organized**: Content categorized into folders
✅ **Linked**: Internal navigation established
✅ **Configured**: Theme and plugins set up

**Next Step**: Enable GitHub Pages in repository settings!

---

**Date**: 2025-10-07
**Version**: 1.0.0
**URL**: https://github.com/red2n/modern-reservation/settings/pages
