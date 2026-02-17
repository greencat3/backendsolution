#!/bin/bash

set -ex

REPO_ROOT_DIR=$(git rev-parse --show-toplevel)
cd $REPO_ROOT_DIR


docker pull postgres:14.21-trixie
docker run --name venn-postgres -e POSTGRES_PASSWORD=mysecretpassword -p 5432:5432 -d postgres
