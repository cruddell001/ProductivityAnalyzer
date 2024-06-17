# Productivity Analyzer

This is a Kotlin command-line utility that uses the Jira and GitHub APIs to query certain performance data for an employee over a given time period. The data includes:

- Jira tickets closed
- Story points completed
- PR's reviewed
- PR's authored

## Prerequisites

- Java 11 or higher
- Jira API token
- GitHub personal access token

## Building the Project

To build the project, run the `build.sh` script located in the `scripts` directory. This script will prompt you to create a `local.properties` file if it does not exist. This file is used to store your Jira and GitHub credentials.

```shellscript
./scripts/build.sh
```

If the build is successful, you will see the message "Build successful! You may now run the app using ./scripts/run.sh".

## Running the Project

To run the project, use the `run.sh` script located in the `scripts` directory. This script checks for the existence of the necessary jar file and the correct Java version before running the jar file with any arguments passed to the script.

```shellscript
./scripts/run.sh
```

## Optional Arguments:
Note: These arguments can be passed to the run script, but are not required.  If any are ommitted, the user will be prompted to enter the information.

### Jira username/email (-j or -jira):
```shellscript
./scripts/run.sh -j <jira_username>
```

### GitHub username (-g or -gh or -github):
```shellscript
./scripts/run.sh -g <github_username>
```

### Days to go back in time (-d or -days):
```shellscript
./scripts/run.sh -d <days>
```

### Load team json file (-t or -team):
```shellscript
./scripts/run.sh -t <team_json_file>
```
Example json format:
```json
{
  "name": "team_name",
  "members": [
    {
      "jiraEmail": "jira_username",
      "githubId": "github_username",
      "level": "SE1"
    }
  ]
}
```
Note: The level field must contain one of: `SE1`, `SE2`, `Senior`, `LeadEngineer`, `TeamLead`, `Manager`

### Full argument list:
```shellscript
./scripts/run.sh -j <jira_username> -g <github_username> -d <days>
```
```shellscript
./scripts/run.sh -t <path_to_json_file> -d <days>
```

## Contributing

Contributions are welcome. Please make sure to update tests as appropriate.
