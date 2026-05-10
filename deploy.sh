#!/bin/sh
./gradlew build

MOD_FOLDER="/home/nscanu/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/1.20.6/minecraft/mods/"
rm -rf $MOD_FOLDER/maudio-1.0.0.jar
cp build/libs/maudio-1.0.0.jar $MOD_FOLDER