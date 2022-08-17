#!/bin/bash

for n in ` find .  -type f -name '*.py'`; do python3 $n; done
