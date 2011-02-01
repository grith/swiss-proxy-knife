## Requirements

* Java version ≥ 1.5
* For the local cert features you (obviously) need a grid certificate
* A properly setup .globus directory. If you used Grix once on your machine this should exist already.
* A directory called .glite/vomses that contains files that describe the VOMS servers to contact. If you used Grix once on your machine this should exist already. 

## Download

Get it from here: [click](https://code.arcs.org.au/hudson/job/JGridAuth-SNAPSHOT/au.org.arcs.auth$swiss-proxy-knife/lastSuccessfulBuild/artifact/au.org.arcs.auth/swiss-proxy-knife/0.4-SNAPSHOT/swiss-proxy-knife.jar)

You'll also need a copy of the bouncy castle security provider in the same directory: [click](http://www.bouncycastle.org/download/bcprov-jdk15-145.jar)

## How to use it

Start it with:

`java -jar swiss-proxy-knife.jar`

And you should see a list of options:

    -a,--action <action>                        What to do with the proxy. Use
                                                one of these options: store-local, myproxy-upload, info
    -d,--myproxy_username <myproxy username>    The username to user when
                                                delegating the proxy to MyProxy
    -h,--help                                   usage information
    -i,--idp <idp name>                         The name of the idp to connect to
    -l,--lifetime <lifetime in hours>           The lifetime of the proxy in hours. Default: 24`
    -m,--mode <mode>                            The mode to create the proxy.
                                                Possible arguments: load-local-proxy, grid-proxy-init, myproxy-login,
                                                shib-proxy-init, shib-list
    -p,--port <myproxy port>                    The myproxy port
    -s,--myproxy_proxyname <myproxy server>     The myproxy server
    -u,--username <username>                    The myproxy- or
                                                shibboleth-username used to get delegated proxy/shibboleth certificate
    -v,--voms_group <voms group>                The voms group you want this
                                                proxy to represent

Be aware that the long-opts don't work at the moment for some reason I wasn't able to figure out yet

Every command basically consists of 3 parts:

* the mode on how to create a proxy (either using the local certificate, getting an existing MyProxy credential or via the slcs-server)
* convert it to a voms proxy or not
* the action: saving the proxy (to the local harddrive or to MyProxy) or displaying info about it. 

Below are example of possible combinations of modes and actions. But you can basically mix all modes with all actions…
Examples

* Create a proxy from the local certificate and store it to the default location (/tmp/x509up_u<uid> on unix):

    java -jar swiss-proxy-knife.jar -m grid-proxy-init -a store-local
    Please provide your private key passphrase: xxxxxxxxxxxxxxx
    Proxy stored to: /tmp/x509up_u1000

* Create a proxy from the local certificate and upload it to myproxy:

    $ java -jar swiss-proxy-knife.jar -m grid-proxy-init -a myproxy-upload -d markus
    Please provide your private key passphrase: xxxxxxxxxxxxxxx
    Please provide the myproxy password to delegate the proxy: xxxxxxxxxx
    MyProxy delegation successful

* Create a proxy from the local certificate, convert it to a voms-proxy and upload it to myproxy:

    $ java -jar swiss-proxy-knife.jar -m grid-proxy-init -a myproxy-upload -v /ARCS/StartUp -d markus-startup
    Please provide your private key passphrase: xxxxxxxxxxxxxxx
    success
    success
    Please provide the myproxy password to delegate the proxy: xxxxxxxxxx
    MyProxy delegation successful

* Retrieve the above proxy again and store it to the local machine:

    java -jar swiss-proxy-knife.jar -m myproxy-login -a store-local -u markus-startup
    Please provide your myproxy password: xxxxxxxxxx
    Proxy stored to: /tmp/x509up_u1000

* Load the proxy that is stored on the local machine and display information about it:

    java -jar swiss-proxy-knife.jar -m load-local-proxy -a info
    subject		:  C=AU,O=APACGrid,OU=VPAC,CN=Markus Binsteiner,CN=proxy,CN=proxy,CN=proxy,CN=proxy
    issuer		:  C=AU,O=APACGrid,OU=VPAC,CN=Markus Binsteiner,CN=proxy,CN=proxy,CN=proxy
    identity	        :  /C=AU/O=APACGrid/OU=VPAC/CN=Markus Binsteiner
    type		:  don't know. if you know how to figure that out, contact makkus+grith@gmail.com
    strength    	:  512
    timeleft	        :  23h, 56min, 34sec

    === VO extension information ===
    issuer		: /C=AU/O=APACGrid/OU=ARCS/CN=vomrs.arcs.org.au
    validity	        : WARNING - Unable to validate the signature of this AC - DO NOT TRUST!
    time left	        : 23:56:24
    holder		: /C=AU/O=APACGrid/OU=VPAC/CN=Markus Binsteiner
    version		: 1
    algorithm	        : MD5 with RSA encryption
    serialNumber	: -1709753703
    attribute    	: /ARCS/StartUp/Role=NULL/Capability=NULL
    attribute           : /ARCS/Role=NULL/Capability=NULL
    attribute    	: /ARCS/Monash/Role=NULL/Capability=NULL
    attribute    	: /ARCS/MonashGeo/Role=NULL/Capability=NULL
    attribute    	: /ARCS/NGAdmin/Role=NULL/Capability=NULL
    attribute    	: /ARCS/SAPAC/Role=NULL/Capability=NULL
    attribute    	: /ARCS/VPAC/Role=NULL/Capability=NULL
    attribute   	: /ARCS/Cloud/Role=NULL/Capability=NULL

* List all available shibboleth IDPs in order to create a proxy using the slcs-service (see next example):

    java -jar swiss-proxy-knife.jar -m shib-list
      AARNet Pty Ltd
      ACU National
      AMMRF
      ANSTO NBI
      ARCS IdP
      Adelaide University
      AuScope
      Australian National University
      CSIRO
      Curtin University of Technology
      ECU
      Education.au Ltd
      Flinders University
      Griffith University
      James Cook University
      MELCOE
      Macquarie University
      Monash University
      Murdoch University
      NCI National Facility
      Queensland University of Technology
      RMIT University
      ScienceDirectTest
      TPAC
      TestFed OpenIdP 2
      The University of Auckland
      The University of Melbourne
      The University of Queensland
      UniSA
      University of Canterbury
      University of Sydney UCC test
      University of Technology, Sydney
      University of Western Australia
      VPAC
      VeRSI VO IdP
      ac3 Research
      eResearchSA
      iVEC IdP

* Create a proxy from the slcs-certificate, convert it to a voms-proxy and store it to the local disk:

    java -jar swiss-proxy-knife.jar -m shib-proxy-init -i "ARCS IdP" -u markus -v /ARCS/StartUp -a myproxy-upload -d markus-shib

    Please provide your institution password: xxxxxxxxxx

    ...

    ...
