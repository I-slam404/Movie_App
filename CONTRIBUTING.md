# Contributing to Movie App

First off, thank you for considering contributing to Movie App! It's people like you that make Movie App a great tool.

## Code of Conduct

This project and everyone participating in it is governed by our commitment to providing a welcoming and inspiring community for all. Please be respectful and constructive in your communications.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates. When creating a bug report, include as many details as possible:

**Bug Report Template:**
```markdown
**Describe the bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '...'
3. Scroll down to '...'
4. See error

**Expected behavior**
What you expected to happen.

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Environment:**
 - Device: [e.g. Pixel 5]
 - OS: [e.g. Android 12]
 - App Version: [e.g. 1.0]

**Additional context**
Add any other context about the problem here.
```

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

**Enhancement Template:**
```markdown
**Is your feature request related to a problem?**
A clear description of what the problem is.

**Describe the solution you'd like**
A clear description of what you want to happen.

**Describe alternatives you've considered**
Other solutions or features you've considered.

**Additional context**
Add any other context or screenshots about the feature request.
```

### Pull Requests

Pull requests are the best way to propose changes to the codebase. We actively welcome your pull requests:

1. Fork the repo and create your branch from `main`
2. If you've added code that should be tested, add tests
3. If you've changed APIs, update the documentation
4. Ensure the test suite passes
5. Make sure your code follows the project's style guidelines
6. Issue that pull request!

## Development Process

### 1. Fork and Clone

```bash
# Fork the repository on GitHub, then:
git clone https://github.com/your-username/Movie_App.git
cd Movie_App
```

### 2. Create a Branch

```bash
git checkout -b feature/amazing-feature
# or
git checkout -b fix/bug-fix
```

**Branch Naming Convention:**
- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation changes
- `refactor/` - Code refactoring
- `test/` - Adding or updating tests
- `chore/` - Maintenance tasks

### 3. Set Up Development Environment

Follow the instructions in [DEVELOPMENT.md](DEVELOPMENT.md) to set up your environment.

**Important:** Create a `local.properties` file with your TMDB API key:
```properties
TMDB_API_KEY=your_api_key_here
```

### 4. Make Your Changes

**Best Practices:**
- Write clean, readable code
- Follow the existing code style
- Add comments for complex logic
- Keep commits atomic and focused
- Write meaningful commit messages

### 5. Write Tests

- Add unit tests for new functionality
- Update existing tests if needed
- Ensure all tests pass before submitting

```bash
./gradlew test
```

### 6. Commit Your Changes

**Commit Message Format:**
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Example:**
```
feat(search): add debouncing to search input

Implement 300ms debounce on search queries to reduce API calls
and improve performance.

Closes #123
```

### 7. Push to Your Fork

```bash
git push origin feature/amazing-feature
```

### 8. Create Pull Request

1. Go to the original repository on GitHub
2. Click "New Pull Request"
3. Select your fork and branch
4. Fill in the PR template
5. Submit the pull request

## Pull Request Guidelines

### PR Template

```markdown
## Description
Briefly describe what this PR does.

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## How Has This Been Tested?
Describe the tests you ran to verify your changes.

## Checklist:
- [ ] My code follows the style guidelines of this project
- [ ] I have performed a self-review of my own code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix is effective or that my feature works
- [ ] New and existing unit tests pass locally with my changes
- [ ] Any dependent changes have been merged and published

## Screenshots (if applicable):
Add screenshots to help explain your changes.
```

### Review Process

1. **Automated Checks**: CI/CD pipeline must pass
2. **Code Review**: At least one maintainer will review
3. **Testing**: Verify functionality works as expected
4. **Discussion**: Address any feedback or requested changes
5. **Merge**: Once approved, your PR will be merged

## Code Style Guidelines

### Kotlin Style

Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// Good
fun calculateTotal(items: List<Item>): Double {
    return items.sumOf { it.price }
}

// Bad
fun calculateTotal(items:List<Item>):Double{
    return items.sumOf{it.price}
}
```

### Formatting

- **Indentation**: 4 spaces (no tabs)
- **Line Length**: Max 120 characters
- **Braces**: Opening brace on same line
- **Imports**: Remove unused imports

**Run formatter:**
```bash
# In Android Studio: Ctrl+Alt+L (Cmd+Option+L on Mac)
```

### Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Class | PascalCase | `MovieRepository` |
| Function | camelCase | `getMovieDetails()` |
| Variable | camelCase | `movieList` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Package | lowercase | `com.islam404.movieapp` |

### Documentation

**Add KDoc for public APIs:**
```kotlin
/**
 * Fetches popular movies from the repository.
 *
 * @return Result containing list of movies or error
 */
suspend fun getPopularMovies(): Result<List<Movie>>
```

## Testing Guidelines

### Unit Tests

```kotlin
@Test
fun `feature should work correctly`() = runTest {
    // Given - Set up test data
    val testData = createTestData()
    
    // When - Execute the code under test
    val result = performOperation(testData)
    
    // Then - Verify the results
    assertEquals(expectedValue, result)
}
```

### Test Coverage

- Aim for >80% code coverage for new code
- All public APIs should have tests
- Test edge cases and error scenarios

### Test Naming

Use descriptive test names:
```kotlin
// Good
@Test
fun `getMovieDetail returns error when movie not found`()

// Avoid
@Test
fun testGetMovieDetail()
```

## Architecture Guidelines

Follow the Clean Architecture pattern:

### Layer Responsibilities

**Presentation Layer:**
- UI logic only
- No business logic
- Call use cases

**Domain Layer:**
- Business logic
- Independent of frameworks
- Pure Kotlin

**Data Layer:**
- Data operations
- Network and database
- Implement repository interfaces

### Dependency Rules

- Presentation → Domain → Data
- Inner layers don't know about outer layers
- Use dependency injection (Hilt)

## Documentation

### Update Documentation

When making changes, update relevant documentation:

- **README.md**: For user-facing changes
- **ARCHITECTURE.md**: For architectural changes
- **API_DOCUMENTATION.md**: For API changes
- **DEVELOPMENT.md**: For development process changes
- **Code comments**: For complex logic

### Documentation Style

- Use clear, concise language
- Include code examples
- Add diagrams for complex concepts
- Keep it up-to-date

## Project Structure

Place files in the correct package:

```
com.islam404.movieapp/
├── data/
│   ├── cache/          # Caching logic
│   ├── local/          # Database
│   ├── mapper/         # Data mappers
│   ├── remote/         # Network
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Domain models
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Use cases
├── presentation/
│   ├── common/         # Shared UI
│   ├── detail/         # Detail screen
│   ├── home/           # Home screen
│   └── theme/          # Theme
└── di/                 # Dependency injection
```

## Resources

### Learning Resources

- [Android Developers](https://developer.android.com/)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

### Tools

- **Android Studio**: IDE
- **Git**: Version control
- **GitHub**: Code hosting
- **Gradle**: Build tool

## Questions?

If you have questions:

1. Check existing documentation
2. Search closed issues
3. Create a new issue with the `question` label
4. Join community discussions

## Recognition

Contributors will be recognized in:
- CONTRIBUTORS.md file
- Release notes
- Project README

Thank you for contributing to Movie App! 🎉
