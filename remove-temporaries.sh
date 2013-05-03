#!/bin/sh
find . -type f \( -name '*.err' -o -name '*.ref' -o -name '*.dot' -o -name '*.s' -o -name '*.bin' \) -delete
