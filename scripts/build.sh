#!/bin/bash

# First, check that local.properties exists.  If not, prompt to create it
if [ ! -f local.properties ]; then
    echo "local.properties not found.  Would you like to create it? Note: you will need a Jira api token and a GitHub token (y/n)"
    read createLocalProperties
    if [ $createLocalProperties == "y" ]; then
        echo "What is the username (email address) associated with your Jira account?"
        read jiraEmail
        echo "What is your Jira api token?"
        read jiraToken
        echo "What is your GitHub personal access token?"
        read gitHubToken
        echo "Finally, what is the Jira base url? (if empty, will use: https://bigcommercecloud.atlassian.net/rest)"
        read jiraBaseUrl
        if [ -z "$jiraBaseUrl" ]; then
            jiraBaseUrl="https://bigcommercecloud.atlassian.net/rest"
        fi
        echo "JIRA_USERNAME=$jiraEmail" > local.properties
        echo "JIRA_TOKEN=$jiraToken" >> local.properties
        echo "GITHUB_TOKEN=$gitHubToken" >> local.properties
        echo "JIRA_BASE_URL=$jiraBaseUrl" >> local.properties
    else
        echo "local.properties not found.  Exiting."
        exit 1
    fi
fi

echo "Building the project..."
# Check if we are in the scripts directory.  If so, move up one level
if [ ! -f build.gradle.kts ]; then
    pwd
    echo "Not on project root level - trying to move up one level"
    cd ..
fi
if [ ! -f ./build.gradle.kts ]; then
    echo "Please run this script from the root of the project.  E.g., ./scripts/build.sh"
    echo "You are on:"
    pwd
    exit 1
fi

./gradlew shadowJar
if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo "You may now run the app using ./scripts/run.sh"
else
    pwd
    echo "Build failed.  Exiting."
    exit 1
fi