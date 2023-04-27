#!/bin/sh
aws --endpoint-url=http://localhost:4566 dynamodb scan --table-name locks-table