# Agama OpenID Connect Project

Use this project to delegate authentication to an external OpenID Connect provider (OP) using the *authorization code flow*.

## How it works at a glance

When the main flow of this project is launched (namely, `io.jans.inbound.openid`) the user's browser is redirected to the authorization page of the configured OP. There authentication takes place and subsequently an access token is obtained to grab user profile data. A user entry is inserted in the local Jans database and a session is created for such "local" user in the Jans Authorization Server. Finally the user's browser is taken to the registered redirect URI.

## Requirements

### Enabled Agama

Ensure Agama is [enabled](https://docs.jans.io/head/admin/developer/agama/engine-bridge-config/#availability) in your Jans Server.

### Custom jar onboarding

- Download the agama inbound identity [jar](https://maven.jans.io/maven/io/jans/agama-inbound/) (suffixed `with-dependencies`) that matches your Jans Server version
- Place the file at `/opt/jans/jetty/jans-auth/custom/libs` in your Jans Server
- Edit the file `/opt/jans/jetty/jans-auth/webapps/jans-auth.xml` to account this file. An easy choice would look like `<Set name="extraClasspath">./custom/libs/*</Set>`
- Restart the authentication server

### External OP settings

Obtain the following from the OP you want to support:

- The authorization endpoint URL
- The token endpoint URL
- The userinfo endpoint 
- The scopes required to obtain user data
- Client credentials (client ID and secret)

In this process, you will be required to supplied a redirect URI, use the following: `https://<jans-server-host-name>/jans-auth/fl/callback`

## Project deployment

- Copy (SCP/SFTP) the gama file of this project to a location in your Jans server
- Connect (SSH) to your Jans Server and open TUI: `python3 /opt/jans/jans-cli/jans_cli_tui.py`
- Navigate to the Agama tab and then select "Upload project". Choose the gama file
- Wait for about one minute and then select the row in the table corresponding to this project
- Press `d` and ensure there were not deployment errors
- Pres ESC to close the dialog

## Project configuration

- Still with the row highlighted, press `c` and choose to export the sample configuration to a file
- Edit a copy of the file according to the OP settings formerly grabbed. They should go under the `io.jans.inbound.oidc_code` section
- Still with the row highlighted, press `c` and choose to import configuration. Supply the file just edited

## Testing

Configure the required in your Jans Server to be able to launch an authentication flow. Actual details may vary but you can resort to a handy browser extension called [jans-tarp](https://github.com/JanssenProject/jans/tree/main/demos/jans-tarp) that will save you a good amount of work.

When testing ensure the following parameters are present in the authorization request:

- `acr_values=agama`
- `agama_flow=io.jans.inbound.openid`
