#!/usr/bin/bash

# This script uninstalls Orion and its dependencies from the system.
# It is intended to be run as a root user.
# It will remove the Orion installation directory.

# Check if the script is run as root
if [ "$EUID" -ne 0 ]; then
    echo "Please run as root."
    exit 1
fi

# Check if Orion is installed using pkgutil
if pkgutil --pkg-info=com.kingjoe.orion > /dev/null 2>&1; then
    echo "Orion is installed using pkgutil. Uninstalling..."
    # remove the Orion installation directory
    if [ -d "/usr/local/orion" ]; then
        echo "Removing /usr/local/orion directory..."
        rm -rf /usr/local/orion
    else
        echo "/usr/local/orion directory does not exist."
    fi

    # remove /usr/local/bin/orion file
    rm -f /usr/local/bin/orion

    pkgutil --forget com.kingjoe.orion
else
    echo "Orion is not installed using pkgutil."
fi
