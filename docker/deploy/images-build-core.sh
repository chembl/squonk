#!/bin/sh

cd images
docker build -t squonk/xwiki -f Dockerfile-xwiki .

# finally move back to the original dir
cd ..


