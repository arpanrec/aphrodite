module.exports = {
    branches: ['main'],
    tagFormat: '${version}',
    plugins: [
        [
            '@semantic-release/commit-analyzer',
            {
                preset: 'angular',
                parserOpts: {
                    noteKeywords: ['BREAKING CHANGE', 'BREAKING CHANGES', 'BREAKING'],
                },
            },
        ],
        [
            '@semantic-release/release-notes-generator',
            {
                preset: 'angular',
                parserOpts: {
                    noteKeywords: ['BREAKING CHANGE', 'BREAKING CHANGES', 'BREAKING'],
                },
                writerOpts: {
                    commitsSort: ['subject', 'scope'],
                },
            },
        ],
        [
            '@semantic-release/exec',
            {
                prepareCmd: [
                    'rm -f .semantic-release-successCmd CHANGELOG.md',
                    './gradlew setVersion -PnewVersion=${nextRelease.version}',
                    './gradlew :aphrodite-backend:clean :aphrodite-backend:bootJar --stacktrace --info -x test',
                ].join(' && '),
                successCmd: [
                    './gradlew :aphrodite-backend:generateOpenApiDocs',
                    './gradlew :aphrodite-backend:publish',
                    'touch .semantic-release-successCmd',
                ].join(' && '),
            },
        ],
        [
            '@semantic-release/changelog',
            {
                changelogFile: 'CHANGELOG.md',
            },
        ],
        [
            '@semantic-release/git',
            {
                assets: ['CHANGELOG.md', 'build.gradle.kts', 'aphrodite-backend/docs/swagger.json'],
                message: 'chore(release): ${nextRelease.version} [skip ci]\n\n${nextRelease.notes}',
            },
        ],
        [
            '@semantic-release/github',
            {
                assets: [
                    {
                        path: 'aphrodite-backend/build/libs/aphrodite-backend-boot-*.jar',
                        label: 'aphrodite-backend-boot.jar',
                    },
                    { path: 'aphrodite-backend/docs/swagger.json', label: 'swagger.json' },
                ],
            },
        ],
    ],
};
