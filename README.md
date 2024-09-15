# Maven Cleaner CLI Tool

## Overview

A simple CLI designed to clean up your local Maven repository. It helps you manage and maintain your Maven cache by allowing you to remove old or redundant version folders, leaving only the necessary ones based on configurable criteria.

## Features

- **Set Local Repository Directory**: Specify the local repository path.
- **Set Default Cleaning Mode**: Choose how to determine which folder to keep.
- **Clean Repository**: Execute the cleaning process based on the chosen mode.

## Installation

### Build the Project

1. **Clone the Repository**

   ```sh
   git clone https://github.com/yourusername/maven-cleaner.git
   cd maven-cleaner
   ```
2. **Build the Project**
   #### Make sure you have Maven installed. Build the project using:
   ```sh
   maven clean package
   ```
   This will generate a `jar` file in the `target` directory.
## Usage
## Set Local Repository Directory
Set the path to your local Maven repository:
```sh
maven-cleaner set-dir /path/to/your/maven/repository
```
## Set Default Cleaning Mode
Set the default cleaning mode (1, 2, or 3):
```sh
maven-cleaner def-mode 1
```
## Clean Repository
Start the cleaning process. You can specify the cleaning mode and whether to reverse the comparator:
```sh
maven-cleaner clean [-clm <cleanMode>] [-r <reversed>]
```
* `-clm` or `--cleanmode`: Specify the cleaning mode (1, 2, or 3). If not provided, the default cleaning mode will be used.
* `-r` or `--reversed`: Reverses the comparator (default is `false`).


