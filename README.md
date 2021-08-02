# Surf OOAPI Gateway Configurator

## Development dependencies

You need [leiningen](https://leiningen.org/) and a recent JDK
(preferably [OpenJDK](http://openjdk.java.net/) to build the
application.

The clojure/java library dependencies will be automatically downloaded
when needed by leiningen.

## Run from the REPL in development

Start the REPL connect your IDE, or run

    lein repl

To start the service from the REPL, type `(start!)` at the REPL
prompt. Use `(restart!)` to reload code changes and restart the
service.

Default url is http://localhost:8080/

## Building a stand-alone jar

The service can be build as a standalone jar:

    lein uberjar

This will generate
`target/uberjar/ooapi-gateway-configurator.jar`.

You will need only a JVM (no leiningen) to run the jar:

    java -jar target/uberjar/ooapi-gateway-configurator.jar

## Configuration

This application is mostly configured using environment variables.

### `HTTP_HOST`

The hostname that the web server binds to. Default is "localhost"

### `HTTP_PORT`

The port that the web server binds to. Default is "8080"

### `AUTH_AUTHORIZE_URI`

The OIDC authorize URI to use for initiating an authorization
flow.

For testing, this should be set to
"https://connect.test.surfconext.nl/oidc/authorize". In production,
"https://connect.surfconext.nl/oidc/authorize" should be used.

### `AUTH_ACCESS_TOKEN_URI`

The OIDC token endpoint to use.

For testing, this should be set to
"https://connect.test.surfconext.nl/oidc/token". In production,
"https://connect.surfconext.nl/oidc/token" should be used.

### `AUTH_CLIENT_ID`

The OIDC client ID to use. This should be
"ooapi-gateway-configurator.surf.nl"

### `AUTH_CLIENT_SECRET`

The OIDC client secret.

### `AUTH_USER_INFO_URI`

The OIDC userinfo_endpoint to use. 

For testing, this should be set to
"https://connect.test.surfconext.nl/oidc/userinfo". In production,
"https://connect.surfconext.nl/oidc/userinfo" should be used.

### `AUTH_REDIRECT_URI`

The callback URI that will be passed to the OIDC server. Make sure
this URI points to the "/oauth2/conext/callback" endpoint.

The default is "/oauth2/conext/callback", which will be resolved to a
full URL based on the request that starts the authentication
process.

The default will not work when the configurator service is behind a
TLS terminating proxy and you need the redirect to use HTTPS. You then
have to specify a full URL to ensure a matching hostname and scheme,
like "https://manage.test.gateway.eduapi.nl/oauth2/conext/callback"

### `AUTH_CONEXT_GROUP_IDS`

A comma separated list of Conext group ids (urns). Access to the
application is granted to users who are members of any of the groups.

For testing, you can use
"urn:collab:group:test.surfconext.nl:nl:surfnet:diensten:beta"

### `GATEWAY_CONFIG_YAML`

The path to the gateway configuration file. This file must exist and
be readable and writable for the application process.

### `PIPELINE`

The name of the pipeline to be configured in the gateway configuration file.

### `WORK_DIR`

Directory where backups and staging information is kept. This
directory needs to exist, be writable and persist through restarts and
reboots. When left unset, the same directory as that of
`GATEWAY_CONFIG_YAML` will be used.

Note: the gateway itself does not need access to this directory.

### `TZ`

Versions are listed with the date and time they are deployed. By
default the system timezone is used to display these timestamps. Use
the `TZ` variable to override this setting.

Note: this is a POSIX environment variable which the JVM honors.

# Configuring logging

Logging uses [logback](https://www.baeldung.com/logback). There is an
example configuration in `dev/logback.xml`, which is loaded in
development. In production you can specify a logback config file by
using

    java -Dlogback.configurationFile=/path/to/logback.xml \
      -jar target/uberjar/ooapi-gateway-configurator.jar

# License

Copyright (C) 2021 SURFnet B.V.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see http://www.gnu.org/licenses/.
