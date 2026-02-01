# Documentation Index

Welcome to the Movie App documentation! This index will help you find the information you need.

## 📚 Documentation Structure

### For Users

1. **[README.md](README.md)** - Start here!
   - Project overview
   - Features list
   - Quick setup guide
   - Tech stack summary
   - Screenshots

### For Developers

2. **[DEVELOPMENT.md](DEVELOPMENT.md)** - Development guide
   - Environment setup
   - Building the project
   - Running tests
   - Debugging tips
   - Gradle configuration
   - Performance optimization

3. **[ARCHITECTURE.md](ARCHITECTURE.md)** - Architecture documentation
   - Clean Architecture overview
   - Layer structure and responsibilities
   - Data flow diagrams
   - Design patterns used
   - Dependency injection setup
   - Best practices

4. **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - API integration guide
   - TMDB API setup
   - Available endpoints
   - Data models
   - Error handling
   - Authentication
   - Rate limiting

### For Contributors

5. **[CONTRIBUTING.md](CONTRIBUTING.md)** - Contribution guidelines
   - How to contribute
   - Code style guidelines
   - PR process
   - Branch naming conventions
   - Testing requirements
   - Documentation standards

### Testing Documentation

6. **[TEST_DOCUMENTATION.md](TEST_DOCUMENTATION.md)** - Comprehensive test guide
   - Test structure
   - Test implementation details
   - Mocking strategies

7. **[TESTING_SUMMARY.md](TESTING_SUMMARY.md)** - Test coverage summary
   - Test results
   - Coverage metrics
   - Test organization

## 🚀 Quick Start Paths

### "I want to use the app"
```
README.md → Setup section → Run the app
```

### "I want to understand the code"
```
README.md → ARCHITECTURE.md → Explore codebase
```

### "I want to contribute"
```
README.md → CONTRIBUTING.md → DEVELOPMENT.md → Make changes
```

### "I want to add a new feature"
```
ARCHITECTURE.md → DEVELOPMENT.md → CONTRIBUTING.md → Code
```

### "I want to understand the API"
```
API_DOCUMENTATION.md → ARCHITECTURE.md (Data Layer)
```

### "I want to run tests"
```
DEVELOPMENT.md (Testing section) → TEST_DOCUMENTATION.md
```

## 📖 Common Tasks

### Setting Up the Project
1. Read [README.md](README.md) - Quick Setup section
2. Follow [DEVELOPMENT.md](DEVELOPMENT.md) - Environment Setup
3. Configure TMDB API key
4. Build and run

### Understanding the Architecture
1. Start with [ARCHITECTURE.md](ARCHITECTURE.md)
2. Understand the three layers (Presentation, Domain, Data)
3. Review data flow diagrams
4. Check dependency injection setup

### Making Your First Contribution
1. Read [CONTRIBUTING.md](CONTRIBUTING.md)
2. Fork and clone the repository
3. Create a feature branch
4. Make your changes following code style guidelines
5. Write tests
6. Submit a pull request

### Adding a New API Endpoint
1. Review [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
2. Add method to `TmdbApi.kt`
3. Create/update DTOs
4. Add mapper functions
5. Update repository
6. Create use case
7. Update ViewModel and UI
8. Write tests

### Debugging Build Issues
1. Check [DEVELOPMENT.md](DEVELOPMENT.md) - Troubleshooting section
2. Clean and rebuild project
3. Verify Gradle configuration
4. Check dependency versions

## 🔍 Finding Specific Information

### Architecture & Design
- **Clean Architecture**: ARCHITECTURE.md
- **MVI Pattern**: ARCHITECTURE.md → State Management
- **Dependency Injection**: ARCHITECTURE.md → Dependency Injection
- **Navigation**: ARCHITECTURE.md → Navigation
- **Error Handling**: ARCHITECTURE.md → Error Handling

### Code Organization
- **Project Structure**: DEVELOPMENT.md → Project Structure
- **Package Organization**: ARCHITECTURE.md → Clean Architecture Layers
- **Naming Conventions**: CONTRIBUTING.md → Naming Conventions

### API & Data
- **TMDB Integration**: API_DOCUMENTATION.md
- **Data Models**: API_DOCUMENTATION.md → Data Models
- **Caching**: ARCHITECTURE.md → Caching Strategy
- **Database**: ARCHITECTURE.md → Data Layer → Local Data Source

### Testing
- **Running Tests**: DEVELOPMENT.md → Testing
- **Writing Tests**: CONTRIBUTING.md → Testing Guidelines
- **Test Coverage**: TESTING_SUMMARY.md
- **Test Structure**: TEST_DOCUMENTATION.md

### Build & Release
- **Building APK**: DEVELOPMENT.md → Building the Project
- **Release Configuration**: DEVELOPMENT.md → Build Variants
- **ProGuard**: DEVELOPMENT.md → Performance Optimization

## 💡 Tips

- **New to the project?** Start with README.md
- **Want to contribute?** Read CONTRIBUTING.md first
- **Setting up?** Follow DEVELOPMENT.md step by step
- **Need API info?** Check API_DOCUMENTATION.md
- **Understanding flow?** Review ARCHITECTURE.md diagrams
- **Writing tests?** See TEST_DOCUMENTATION.md examples

## 🆘 Getting Help

If you can't find what you're looking for:

1. **Search within documentation**: Use Ctrl+F / Cmd+F
2. **Check existing issues**: GitHub Issues tab
3. **Ask a question**: Create a new issue with `question` label
4. **Join discussions**: GitHub Discussions

## 📝 Documentation Maintenance

This documentation is maintained by the project contributors. If you find:
- Outdated information
- Missing details
- Errors or typos
- Opportunities for improvement

Please submit a PR or create an issue!

## 🔗 External Resources

- [Android Developer Guides](https://developer.android.com/guide)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose/documentation)
- [Kotlin Language Docs](https://kotlinlang.org/docs/home.html)
- [TMDB API Docs](https://developers.themoviedb.org/3)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

**Last Updated**: February 2026

**Documentation Version**: 1.0
