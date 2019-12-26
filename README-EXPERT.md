# whirlpool-client-cli for EXPERTS


## Expert usage

#### Debugging
- ```--debug```: debug logs
- ```--debug-client```: more debug logs
- ```--dump-payload```: dump pairing-payload of current wallet and exit


#### Custom Tor configuration
Tor should be automatically detected, installed or configured. You can customize it for your needs:
```
cli.torConfig.executable = /path/to/bin/tor
```
- Use `auto` to use embedded tor, or detecting a local Tor install when your system is not supported.
- Use `local` to detect a local tor install.
- Use custom path to `tor` binary to use your own tor build.

```
cli.torConfig.onionServer = true
cli.torConfig.onionBackend = true
```
When tor enabled, connect to whirlpool server or wallet backend through:
- `true`: Tor hidden services 
- `false`: clearnet over Tor


## Whirlpool integration

#### Authenticate on startup
You can authenticate in several ways:
- ```--authenticate```: manually type your passphrase on startup
- ```--listen```: use the GUI or API to authenticate remotely


For security reasons, you should not store your passphrase anywhere. If you really need to automate authentication process, use this at your own risk:
```
export PP="mypassphrase"
echo $PP|java -jar whirlpool-client-cli-x-run.jar --authenticate
```


#### Configuration override
Configuration can be overriden in whirlpool-cli-config.properties (see default configuration in [src/main/resources/application.properties]).

Or with following arguments:
- ```--scode```: scode to use for tx0
- ```--tx0-max-outputs```: tx0 outputs limit
- ```--auto-tx0=[poolId]```: run tx0 from deposit utxos automatically
- ```--auto-mix=[true/false]```: mix premix utxos automatically



## Build instructions
Build with maven:

```
cd whirlpool-client-cli
mvn clean install -Dmaven.test.skip=true
```