# Kanji-Together

## Gemini configuration

Kanji Together calls the Google Gemini API. To avoid leaking credentials, never
commit API keys to source control. Instead, provide your key at runtime using
one of the following options:

1. Set the environment variable before running the app:
   ```bash
   export GEMINI_API_KEY=your-key-here
   ./mvnw spring-boot:run
   ```
2. Or create a local (untracked) `application.properties`/`application.yml` file
   and add `gemini.api.key=your-key-here`.

If you suspect a key has been exposed (for example, receiving a `403 Forbidden`
with a warning that the key leaked), revoke it in the Google AI Studio console
and generate a new one before continuing.
