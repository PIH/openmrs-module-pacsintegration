
# Configuring Mirth

## Installing Via Docker

To install Mirth, with it's backing database, you can use the docker-compose file in this directory.  This will create a Mirth container and a MySQL container.  

Steps:

- Copy the `docker-compose-mirth.yml` and `.env` files to the directory on your machine where you want to install Mirth.
- Edit the `.env` file to set the MySQL username and password, as well as the mirth inbound port and the directory to use for docker data. (Note that the username/password is only used internally within this Docker network, so only need to be set here, though you will need them if you want to connect to the Mirth MySQL database remotely for any reason.)
- From this directory, create the directories `${DOCKER_DATA_DIRECTORY}/mirthconnect/appdata`, `${DOCKER_DATA_DIRECTORY}/mirthconnect/logs`, and `${DOCKER_DATA_DIRECTORY}/mirthconnect/mysql` so they can be properly mapped into the container.
- Run `docker compose -f docker-compose-mirth.yml up` to start the Mirth and MySQL containers.
- If you see error messages (likely) about the Mirth container being unable to create files in the "appdata" and "logs" directories:
  - In another terminal, run "docker exec -it docker-mirthconnect-1 id" and note the PID associated with the Mirth user
  - Change the owner of these two directories to that user:  `sudo chown {PID} ${DOCKER_DATA_DIRECTORY}/mirthconnect/appdata ${DOCKER_DATA_DIRECTORY}/mirthconnect/logs`
  - Start and stop the containers

## Connecting to Mirth using the Mirth Connect Client

To interact with the Mirth Client, you need to run the Mirth Connect Administrator, via the Mirth Connect Administrator Launcher.

- Download and unzip v1.4.2 of the Mirth Connect Administrator Launcher: https://www.nextgen.com/products-and-services/nextgen-connect-integration-engine-downloads
- (If v1.4.2 if is no longer the most recent version, feel free to try testing with the latest release, but if that doesn’t work, you should be able to find it here: https://mirthdownloadarchive.s3.amazonaws.com/mcal-downloads.html)
- Open a terminal window and execute export GDK_SCALE=2 (to increase font size to be readable)
- From that terminal, Run the launcher by executing the “launcher” file (ie ./launcher) 
- Instead of using the bundled version of Java, select “Custom”, and point to a local Java JDK for Java 11 or higher (this is only necessary to increase the font size to be readable)
- Set the URL to 'https://localhost:8443/' if installed locally, otherwise substitute "localhost" with the domain name of the server you installed Mirth on, and click "Launch".
- A new window should open, and you will be prompted to login. The default username and password are both "admin".
- If you are installing for the first time, it will prompt you to create a new password.  If installing anywhere but locally, please make sure to either use the password in Bit Warden or to save the new password in Bit Warden.

## Installing the channels

To install the channels, you will need to import the three channels from the `mirth/channels` directory in this repository.  The channels are stored in XML files, and can be imported into Mirth using the Mirth Connect Administrator:
- Click on the "Channels" tab
- Click on the "Import Channel" button and pick a channel to import
- Repeat for all three channels
https://docs.nextgen.com/bundle/Mirth_User_Guide_41/page/connect/connect/topics/c_Configuration_MapConfiguration_Map_connect_ug.html
Note that there some environmental variables/secrets that must be set.  For now, we will either manually set them in the channel xml files before import (make sure not to commit them to source control) or manually update them via the Mirth Connect Administrator after import.
- openmrs_mysql_username: mysql username that Mirth will use to connect to the OpenMRS database
- openmrs_mysql_password: mysql password that Mirth will use to connect to the OpenMRS database
- mirth_inbound_port: port that Mirth will listen on for incoming messages; this should match the value set in .env file for MIRTH_INBOUND_PORT
- openmrs_inbound_port: port that OpenMRS is listening on for incoming messages; this should match "pacsintegration.hl7ListenerPort" global property in OpenMRS
- pacs_url: the url of the PACS server to connect to
- pacs_inbound_port: the port where the PACS server is listening for inbound messages from Mirth

Once you have installed the channels, click "Redeploy Channel" to deploy the channels.