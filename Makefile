# Basic Makefile just to make creating containers a bit easier outside of
# Jenkins itself (see also: Jenkinsfile)

plugindata:
	mvn -PgeneratePluginData

war: plugindata
	mvn -B clean verify

build: war plugindata
	docker build -t jenkinsciinfra/plugin-site deploy


.PHONY: build war plugindata
