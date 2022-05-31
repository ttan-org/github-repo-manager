Axon Ivy ESCROW Backup
======================

This README file describes how to use this the Axon Ivy backup file contained in this folder.

The file ivy_escrow.__DATE__.tar.gz contains, in compressed form, a full backup of all relevant repositories for following products:

- Axon Ivy Designer versions LTS 7.0.x, LTS 8.0.x, LE 9.x
- Axon Ivy Engine versions LTS 7.0.x, LTS 8.0.x, LE 9.x

with a full history of all files and branches.


How to use the backup file
--------------------------

In order to inspect the source code for these products please follow these steps:

1. Install a Git client for your operating system.
2. Unpack the ivy_escrow.__DATE__.tar.gz file, either with a program like 7-Zip (https://www.7-zip.org/download.html) or with following command:
     tar -xzf ivy_escrow.__DATE__.tar.gz
3. For version LTS __versionLTS70__:
     - With your Git client, run the following command in sub-directories /ivy-core-branch-70
         git checkout __versionLTS70__
   For version LTS __versionLTS80__:
     - With your Git client, run the following command in sub-directories /ivy-core
         git checkout __versionLTS80__
   For version LE __versionLE__:
     - With your Git client, run the following command in sub-directories /ivy-core
         git checkout __versionLE__

All source code will now be visible.
