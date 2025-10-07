# GitHub Workflows

This directory contains GitHub Actions workflows for CI/CD automation.

## Available Workflows

### 1. Build Java Services (`build-java-services.yml`)

**Purpose:** Build and test all Java microservices

**Type:** ⚡ Automatic (on push/PR)

**Triggers:**
- Push to `main` or `develop` branches (when Java files change)
- Pull requests to `main` or `develop` branches
- Manual trigger via workflow dispatch

**What it does:**
1. ✅ Sets up JDK 17 (Temurin distribution)
2. ✅ Caches Maven dependencies for faster builds
3. ✅ Builds shared backend utilities
4. ✅ Builds infrastructure services:
   - Config Server
   - Eureka Server
   - Gateway Service
5. ✅ Builds business services:
   - Reservation Engine
   - Payment Processor
   - Rate Management
   - Availability Calculator
   - Analytics Engine
   - Batch Processor (if implemented)
6. ✅ Runs unit tests for all services
7. ✅ Generates build summary report
8. ✅ Uploads JAR artifacts (7 days retention)
9. ✅ Uploads test reports (7 days retention)

**Build Matrix:**
- Java Version: 17
- OS: Ubuntu Latest

**Artifacts:**
- `java-services-jars-{sha}`: All compiled JAR files
- `test-reports-{sha}`: All test execution reports

**Cache Strategy:**
- Maven local repository cached based on `pom.xml` changes
- Speeds up subsequent builds significantly

## Manual Trigger

You can manually trigger the workflow from GitHub Actions tab:

1. Go to Actions → Build Java Services
2. Click "Run workflow"
3. Select branch
4. Click "Run workflow" button

## Viewing Results

### Build Summary
Check the workflow summary for quick status of each service:
- ✅ Green checkmark: Build succeeded
- ❌ Red X: Build failed

### Artifacts
Download compiled JARs or test reports from the workflow run page.

### Logs
View detailed logs for each build step to troubleshoot issues.

## Build Order (IMPORTANT!)

The workflow builds in this specific order:

1. **Parent POM** (`mvn install -N`) - Must be first!
2. **Backend Utils** - Shared utilities
3. **Infrastructure Services** - Config, Eureka, Gateway
4. **Business Services** - Domain services

**Why this order?** Child modules depend on the parent POM being in the local Maven repository first. The `-N` flag installs only the parent without recursively building children.

## Local Development

To replicate the CI build locally:

```bash
# Use dev.sh script (recommended)
./dev.sh start

# Or manually build all services
cd apps/backend/java-services

# Build infrastructure
cd infrastructure
for service in config-server eureka-server gateway-service; do
  cd $service && mvn clean package && cd ..
done

# Build business services
cd ../business-services
for service in reservation-engine payment-processor rate-management; do
  cd $service && mvn clean package && cd ..
done
```

## Adding New Services

When adding a new Java service:

1. Add the service directory to `apps/backend/java-services/`
2. The workflow will automatically detect and build it
3. No workflow changes needed (auto-discovery)

## Troubleshooting

### Build Failures

**Maven dependency issues:**
- Check if `libs/shared/backend-utils` builds successfully
- Verify all dependencies are in Maven Central or configured repositories

**Out of memory errors:**
- Maven build uses default JVM settings
- Large services may need increased heap size in `pom.xml`

**Test failures:**
- Tests run with `-DskipTests` by default for faster builds
- Full test suite runs separately in test step
- Check test reports artifact for details

## Environment Variables

No environment variables required for basic builds.

For production deployments, you may need:
- `MAVEN_OPTS`: JVM options for Maven
- `SPRING_PROFILES_ACTIVE`: Spring Boot profile

## Cache Management

Maven cache is automatically managed:
- Cache key: `${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}`
- Updates when any `pom.xml` file changes
- Restore keys fall back to latest cache

To clear cache:
1. Go to repository Settings → Actions → Caches
2. Delete relevant cache entries

## Performance

Typical build times (without cache):
- First build: ~10-15 minutes
- Subsequent builds (with cache): ~3-5 minutes

Services built in parallel where possible.

---

### 2. Deploy GitHub Pages (`deploy-pages-adhoc.yml`)

**Purpose:** Deploy documentation and reports to GitHub Pages

**Type:** 🎯 Ad-hoc (manual trigger only)

**Why Ad-hoc?**
- Prevents unnecessary deploys on every commit
- Gives you full control over when to publish
- Reduces Actions minutes usage
- Documentation changes less frequently than code

**Triggers:**
- ✅ Manual only via workflow dispatch
- ❌ Never automatic

**Manual Trigger Options:**

Go to Actions → Deploy GitHub Pages → Run workflow

You can choose:
- **Environment**: `production` or `staging`
- **Deploy API Docs**: Include GraphQL schemas (default: true)
- **Deploy Architecture**: Include diagrams and ADRs (default: true)
- **Deploy Reports**: Include test/coverage reports (default: false)

**What it deploys:**
1. 🏠 Beautiful landing page with navigation
2. 📚 Project documentation from `/docs`
3. 🔌 API documentation (GraphQL schemas)
4. 🏗️ Architecture diagrams and ADRs
5. 📊 Test reports (optional)

**Deployment URL:**
- Will be shown in workflow output
- Typically: `https://{username}.github.io/{repo-name}`

**First-time Setup:**
1. Go to repository Settings → Pages
2. Source: Deploy from GitHub Actions
3. Run the workflow once to enable Pages

**When to Deploy:**
- After major documentation updates
- After completing new features (API docs)
- After sprint reviews (for stakeholders)
- Before demos or presentations
- When you want to share architecture decisions

**Quick Deploy Command:**
```bash
# Via GitHub CLI (if installed)
gh workflow run deploy-pages-adhoc.yml \
  -f environment=production \
  -f deploy_docs=true \
  -f deploy_architecture=true \
  -f deploy_reports=false
```

---

## Best Practices

1. ✅ Always use `./dev.sh` for local operations
2. ✅ Run `mvn clean install` before committing
3. ✅ Keep `pom.xml` files up to date
4. ✅ Write unit tests for new features
5. ✅ Check workflow status before merging PRs
6. ✅ Deploy Pages only when documentation changes
7. ✅ Use ad-hoc workflows for non-critical deployments

## Security

- No secrets required for building
- Artifacts are private to repository
- 7-day retention minimizes storage costs
- Pages deployment uses GITHUB_TOKEN

## Support

For issues with workflows:
1. Check workflow logs for error details
2. Run local test: `./scripts/test-github-build.sh`
3. Read [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for common issues
4. Verify local build works: `./dev.sh start`
5. Check MCP server rules: Ask Copilot for guidance
6. For Pages issues: Check repository Settings → Pages

## Common Issues

### "Could not find artifact java-services-parent:pom"
**Solution:** Parent POM must be installed first. This is now automatic in the workflow.
See [TROUBLESHOOTING.md](TROUBLESHOOTING.md#1--could-not-find-artifact-java-services-parentpom) for details.

### "Unsupported class file major version"
**Solution:** Java version mismatch. Workflow uses Java 21 to match project requirements.
See [TROUBLESHOOTING.md](TROUBLESHOOTING.md#2--unsupported-class-file-major-version) for details.
4. Review pom.xml dependencies
