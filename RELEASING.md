0. Pick a version number to use.
1. Ensure CHANGELOG.md is up to date
2. Update gradle/libs.versions.toml to contain the new version number
3. git commit && git tag
4. Edit build.gradle.kts to comment out debug mode on the modrinth publish
5. Generate a Modrinth PAT and set MODRINTH_TOKEN in your environment
6. Run `./gradlew modrinth`
7. Revert build.gradle.kts.
