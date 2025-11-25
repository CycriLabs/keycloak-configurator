#!/bin/bash

# Function to display usage information
function show_usage {
  echo "Usage: $0 [OPTIONS]"
  echo "Manage Keycloak Docker container"
  echo ""
  echo "Options:"
  echo "  --start          Start the Keycloak container"
  echo "  --stop           Stop and remove the Keycloak container (including volumes)"
  echo "  --dev            Start Keycloak configurator via maven in dev mode"
  echo "  --help           Display this help message"
  echo ""
  echo "Example: $0 --start"
}

# Function to start Keycloak container
function start_keycloak {
  echo "Starting Keycloak container..."
  docker compose up -d keycloak --wait

  # Check if container is running
  if docker ps | grep -q keycloak; then
    echo "Keycloak container is now running"
  else
    echo "Error: Failed to start Keycloak container"
    exit 1
  fi
}

# Function to stop and remove Keycloak container
function stop_keycloak {
  echo "Stopping Keycloak container..."
  if docker ps -a | grep -q keycloak; then
    echo "Removing Keycloak container and volumes..."
    docker compose down --volumes --remove-orphans
  else
    echo "Keycloak container is not running"
  fi
}

# Function to start Keycloak configurator in dev mode
function start_dev {
  echo "Starting Keycloak configurator in dev mode..."
  mvn quarkus:dev -Dquarkus.args="configure -s http://localhost:8080 -u keycloak -p root -c ./src/test/resources/configuration -t client-role" -Dgithub
}

# Check if no arguments were provided
if [ $# -eq 0 ]; then
  show_usage
  exit 1
fi

# Process command line arguments
while [ $# -gt 0 ]; do
  case "$1" in
    --start)
      start_keycloak
      shift
      ;;
    --stop)
      stop_keycloak
      shift
      ;;
    --dev)
      start_dev
      shift
      ;;
    --help)
      show_usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      show_usage
      exit 1
      ;;
  esac
done

exit 0
