# Contributing

This is primarily a tutorial / reference repository. PRs are welcome for:

- Bug fixes
- Documentation improvements
- New tutorial-aligned features (open an issue first to discuss scope)

## Local checks before opening a PR

```bash
mvn verify
docker build -t sample-web-app:dev .
```

## Style

- Java 21 features are fair game (records, pattern matching, virtual threads where it makes sense).
- Constructor injection for Spring beans (no `@Autowired` on fields).
- Use `@Slf4j`-style logging via SLF4J. No `System.out`.
- Public REST DTOs go through Bean Validation (`jakarta.validation`).
